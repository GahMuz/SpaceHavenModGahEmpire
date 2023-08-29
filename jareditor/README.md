# JarEditor
Enable modification to spacehaven working on bytecode directly

Edit jarPath variable in class com.gah.empire.jareditor.Launch
Then execute the main method

Then execute the maven command:
`mvn install:install-file -DgroupId="fi.bugbyte" -DartifactId=spacehaven -Dpackaging=jar -Dversion=1.0.0 -Dfile="</path/to/spacehaven.jar>" -DgeneratePom=true`  

## Actual command
`install:install-file -DgroupId="fi.bugbyte" -DartifactId=spacehaven -Dpackaging=jar -Dversion=1.0.0 -Dfile="C:/Program Files (x86)/Steam/steamapps/common/SpaceHaven/spacehaven.jar" -DgeneratePom=true` 
