import com.typesafe.sbt.packager.docker._

packageName in Docker := "web"
version in Docker := "latest"
dockerRepository := Some("registry.heroku.com/pokey")

dockerCommands := dockerCommands.value.filterNot {
  case ExecCmd("CMD", _*) => true
  case ExecCmd("ENTRYPOINT", _*) => true
  case _ => false
}

dockerCommands ++= Seq(
  ExecCmd("CMD", "sh", "-c", "bin/pokey -Dhttp.port=$PORT")
)
