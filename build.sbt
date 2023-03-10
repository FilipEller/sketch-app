name := "sketch-app"

version := "0.1"

scalaVersion := "2.13.2"

// Add dependency on ScalaTest
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.11"

// Add dependency on ScalaFX library
libraryDependencies += "org.scalafx" %% "scalafx" % "14-R19"
libraryDependencies += "org.scalafx" % "scalafx-extras_2.13" % "0.5.0"

// Add dependency on uJson / uPickle
libraryDependencies += "com.lihaoyi" %% "upickle" % "1.6.0"

// Add dependency on os-lib
libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.8.1"

// Determine OS version of JavaFX binaries
lazy val osName = System.getProperty("os.name") match {
case n if n.startsWith("Linux") => "linux"
case n if n.startsWith("Mac") => "mac"
case n if n.startsWith("Windows") => "win"
case _ => throw new Exception("Unknown platform!")
}

// Add JavaFX dependencies, as these are required by ScalaFx
lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
libraryDependencies ++= javaFXModules.map( m=>
"org.openjfx" % s"javafx-$m" % "14.0.1" classifier osName
)