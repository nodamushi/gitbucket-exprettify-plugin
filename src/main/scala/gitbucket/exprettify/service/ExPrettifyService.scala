package gitbucket.exprettify.service
import gitbucket.exprettify.model.{ExPrettifyLanguage,ExPrettifyPattern}
import gitbucket.exprettify.model.Profile._
import gitbucket.exprettify.model.Profile.profile.blockingApi._


trait ExPrettifyService{



  def addLanguage(userName:String,repositoryName:String,language:String,source:String)(implicit s:Session) = 
    ExPrettifyLanguages.insert(ExPrettifyLanguage(userName,repositoryName,language,source))

  def addLanguages(userName:String,repositoryName:String, data :(String,String)*)(implicit s:Session)=
    ExPrettifyLanguages.insertAll(
      data.map(
        d=>ExPrettifyLanguage(userName,repositoryName,d._1,d._2)):_*)


  def clearLanguages(userName:String,repositoryName:String)(implicit s:Session)=
    ExPrettifyLanguages
      .filter( t =>
               (t.userName === userName.bind) &&
               (t.repositoryName === repositoryName.bind))
      .delete

  def copyLanguages(dstUser:String,dstRepository:String,srcUser:String,srcRepository:String)(implicit s:Session):Unit = {
    clearLanguages(dstUser, dstRepository)
    addLanguages(dstUser,dstRepository,
      ExPrettifyLanguages
        .filter( t =>
          (t.userName === srcUser.bind) &&
            (t.repositoryName === srcRepository.bind))
        .map(t=> t.language -> t.source)
        .list:_*
    )
  }

  def moveLanguages(dstUser:String,dstRepository:String,srcUser:String,srcRepository:String)(implicit s:Session):Unit = {
    ExPrettifyLanguages
      .filter( t =>
        (t.userName === srcUser.bind) &&
          (t.repositoryName === srcRepository.bind))
      .map(t=> t.userName -> t.repositoryName)
      .update(dstUser->dstRepository)
  }


  def getLanguage(userName:String,repositoryName:String)(implicit s:Session) = 
    ExPrettifyLanguages
      .filter(t=>
        (t.userName === userName.bind) &&
        (t.repositoryName === repositoryName.bind))
      .sortBy( t => t.language )
      .list

  def getLanguage(userName:String,repositoryName:String,language:String)(implicit s:Session) =
    ExPrettifyLanguages
      .filter( t=>
        (t.userName === userName.bind) &&
        (t.repositoryName === repositoryName.bind) &&
        (t.language === language.bind))
      .firstOption
  
  def getLanguageSource(userName:String,repositoryName:String,language:String)(implicit s:Session) = getLanguage(userName,repositoryName,language) match {
    case Some(ExPrettifyLanguage(_, _, _, "")) => Some(None)
    case Some(ExPrettifyLanguage(_, _, _, source)) => Some(source)
    case _ => None
  }




  def addPattern(userName:String,repositoryName:String,pattern:String,language:String)(implicit s:Session) = 
    ExPrettifyPatterns.insert(ExPrettifyPattern(userName,repositoryName,pattern,language))

  def addPatterns(userName:String,repositoryName:String, data :(String,String)*)(implicit s:Session)=
    ExPrettifyPatterns.insertAll(
      data.map(
        d=>ExPrettifyPattern(userName,repositoryName,d._1,d._2)):_*)


  def clearPatterns(userName:String,repositoryName:String)(implicit s:Session)=
    ExPrettifyPatterns
      .filter( t =>
               (t.userName === userName.bind) &&
               (t.repositoryName === repositoryName.bind))
      .delete


  def copyPatterns(dstUser:String,dstRepository:String,srcUser:String,srcRepository:String)(implicit s:Session):Unit = {
    clearPatterns(dstUser, dstRepository)
    addPatterns(dstUser,dstRepository,
      ExPrettifyPatterns
        .filter( t =>
          (t.userName === srcUser.bind) &&
          (t.repositoryName === srcRepository.bind))
        .map(t=> t.pattern -> t.language)
        .list:_*
    )
  }

  def movePatterns(dstUser:String,dstRepository:String,srcUser:String,srcRepository:String)(implicit s:Session):Unit = {
    ExPrettifyPatterns
      .filter( t =>
        (t.userName === srcUser.bind) &&
          (t.repositoryName === srcRepository.bind))
      .map(t=> t.userName -> t.repositoryName)
      .update(dstUser->dstRepository)
  }




  def getPattern(userName:String,repositoryName:String)(implicit s:Session) = 
    ExPrettifyPatterns
      .filter( t => 
        (t.userName === userName.bind) && 
        (t.repositoryName === repositoryName.bind))
      .sortBy( t => t.language )
      .list


  def getPattern(userName:String,repositoryName:String,pattern:String)(implicit s:Session) = 
    ExPrettifyPatterns
      .filter( t =>
        (t.userName === userName.bind) &&
        (t.repositoryName === repositoryName.bind)&&
        t.pattern.endsWith(pattern))
      .firstOption
  
}
