package gitbucket.exprettify.model

import gitbucket.core.model._

case class ExPrettifyLanguage(
  userName: String,
  repositoryName: String,
  language : String,
  source  : String)


case class ExPrettifyPattern(
  userName: String,
  repositoryName: String,
  pattern : String,
  language  : String)

object Profile extends CoreProfile
    with ExPrettifyPatternComponent
    with ExPrettifyLanguageComponent


trait ExPrettifyLanguageComponent {self:gitbucket.core.model.Profile=>
  import profile.api._
  lazy val ExPrettifyLanguages =  TableQuery[ExPrettifyLanguages]

  class ExPrettifyLanguages(tag:Tag) extends Table[ExPrettifyLanguage](tag,"EXPRETTIFY_LANGUAGE"){
    val userName = column[String]("USER_NAME")
    val repositoryName = column[String]("REPOSITORY_NAME")
    val language = column[String]("LANGUAGE")
    val source = column[String]("SOURCE")

    def * = (userName,repositoryName,language,source)<>(ExPrettifyLanguage.tupled,ExPrettifyLanguage.unapply)
  }
}
  
trait ExPrettifyPatternComponent {self:Profile=>
  import profile.api._
  lazy val ExPrettifyPatterns = TableQuery[ExPrettifyPatterns]

  class ExPrettifyPatterns(tag:Tag) extends Table[ExPrettifyPattern](tag,"EXPRETTIFY_PATTERN"){
    val userName = column[String]("USER_NAME")
    val repositoryName = column[String]("REPOSITORY_NAME")
    val pattern = column[String]("PATTERN")
    val language = column[String]("LANGUAGE")

    def * = (userName,repositoryName,pattern,language)<>(ExPrettifyPattern.tupled,ExPrettifyPattern.unapply)
  }
}
  

