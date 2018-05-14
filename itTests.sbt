lazy val startPGTask = TaskKey[Unit]("startContainer", "Start postgres in a docker")
startPGTask := {
  import sys.process._
  "docker-compose up -d".!
  println("containers are ready!")
}
test in IntegrationTest := ((test in IntegrationTest) dependsOn startPGTask).value

lazy val stopPGTask = TaskKey[Unit]("stopContainer", "Stop postgres docker")
stopPGTask := {
  import sys.process._
  s"docker-compose down".!
  println(s"docker containers destroyed")
}
stopPGTask := (stopPGTask triggeredBy (test in IntegrationTest)).value
