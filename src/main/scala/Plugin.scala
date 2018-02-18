import io.github.gitbucket.solidbase.model.Version
import io.github.gitbucket.solidbase.migration.LiquibaseMigration
import gitbucket.core.plugin.Link
import gitbucket.core.controller.Context
import gitbucket.core.service.RepositoryService.RepositoryInfo
import gitbucket.exprettify.controller._

class Plugin extends gitbucket.core.plugin.Plugin {
  override val pluginId: String = "gitbucket-exprettify-plugin"
  override val pluginName: String = "External Prettify Plugin"
  override val description: String = "Support external syntax scripts of google code prettify."
  override val versions: List[Version] = List(
    new Version("0.0.1", new LiquibaseMigration("spl/exprettify.xml"))
  )

  override val controllers = Seq(
    "/*" -> new ExPrettifyController
  )
  override val javaScripts = Seq(
      "/[^/]+/[^/]+/(blob|blame)/.*$"->"""
(function(){
  var s  = location.pathname.split("/");
  var p  = "/exprettify/js/"+s[1]+ "/"+s[2]+"/"+s[s.length-1];
  document.write("<script src='"+p+"' type='text/javascript'></"+"script>");
})();
"""
  )

  override val repositorySettingTabs = Seq(
    (repository:RepositoryInfo,context:Context) => Some(Link("exprettify","ExPrettify","settings/exprettify"))
  )


}
