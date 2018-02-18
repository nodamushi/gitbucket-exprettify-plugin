package gitbucket.exprettify.controller

import gitbucket.exprettify.html
import gitbucket.exprettify.util._
import gitbucket.core.controller.ControllerBase
import gitbucket.core.service.{ AccountService, RepositoryService }
import gitbucket.core.util.{ OwnerAuthenticator, ReferrerAuthenticator }
import gitbucket.exprettify.service.ExPrettifyService
import gitbucket.core.util.Implicits._


class ExPrettifyController
    extends ExPrettifyControllerBase
    with AccountService
    with OwnerAuthenticator
    with RepositoryService
    with ReferrerAuthenticator
    with ExPrettifyService

object ExPrettifyControllerBase{
  val languageReg = """([a-zA-Z-_]+)""".r
  val sourceReg ="""((?:https?:\/|ftp:\/)?(?:\/[-_.!~*\'()a-zA-Z0-9;\/?:\@&=+\$,%#]+))""".r
  val patternReg= """([^\\/:*?"<>|\r\n]+)""".r
}
trait ExPrettifyControllerBase extends ControllerBase {
  self: AccountService with RepositoryService with ReferrerAuthenticator with OwnerAuthenticator with ExPrettifyService =>
  import ExPrettifyControllerBase._
  get("/exprettify/js/:owner/:repo/:file"){
    val o = params("owner")
    val r = params("repo")
    val file = params("file")
    val language = getPattern(o,r)
      .filter(t => file.endsWith(t.pattern))
      match {
        case Seq() => findDefaultExtentionLanguage(file)
        case Seq(a) => Some(a.language)
        case a =>Some(a.maxBy(t=>t.pattern.length).language)
      }
    val source = language match {
      case Some(l) => getLanguage(o, r, l) match {
        case Some(o) => Some(if(o.source.isEmpty)None else Some((o.source)))
        case _ => findBuiltInLanguageSource(l)
      }
      case _ => None
    }

    language match{
      case Some(l) =>{
        val lcode = s"""e.className+=" lang-$l";"""

        val code = source match {
          case Some(Some(src)) =>s"""document.write("<script src='$src' type='text/javascript'></" + "script>");$lcode"""
          case Some(None) => lcode
          case _ => ""
        }
        if(code.isEmpty) s"//empty language=$l"
        else s"""(function(){var e=document.querySelector("pre.prettyprint");if(e){$code}})();"""
      }
      case _ => "//empty"
    }
  }

  get("/:owner/:repository/settings/exprettify")(ownerOnly { repository =>
    val u = repository.repository.userName
    val r = repository.repository.repositoryName
    val l = getLanguage(u, r)
    val p = getPattern(u, r)
    html.option(repository,l,p,flash.get("info"))
  })

  post("/:owner/:repository/settings/exprettify/:mode")(ownerOnly { repository =>
    val u = repository.repository.userName
    val r = repository.repository.repositoryName
    params("mode") match {
      case "copy"    =>{
        val fowner = params("fowner")
        val frepo  = params("frepo")
        getRepository(fowner, frepo) match {
          case Some(_) => {
            copyLanguages(u,r,fowner,frepo)
            copyPatterns(u, r, fowner, frepo)
            (true,"")
          }
          case _ => (false,s"Repository:$fowner/$frepo not found")
        }
      }
      case "default" => {
        val patt = params("pattern").split('|')
          .map(_.split('^') match{
            case Array(patternReg(pattern),languageReg(language)) => Some(pattern->language)
              //TODO show error
            case _ => None})
          .collect{case Some(v) => v}
          .toMap
          .toSeq
        clearPatterns(u, r)
        if(!patt.isEmpty){
          addPatterns(u, r, patt:_*)
        }
        val lang = params("language").split('|')
          .map(_.split('^') match{
            case Array(languageReg(language),sourceReg(src)) => Some(language->src)
            //TODO show error
            case a =>None})
          .collect{case Some(v) => v}
          .toMap
          .toSeq
        clearLanguages(u, r)
        if(!lang.isEmpty){
          addLanguages(u,r,lang:_*)
        }
        (true,"")
      }
      case _ => (false,"Unknown mode")
    }
    //TODO show error
    redirect(s"/${repository.owner}/${repository.name}/settings/exprettify")
  })

}


