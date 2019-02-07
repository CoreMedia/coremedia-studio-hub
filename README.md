# CoreMedia Studio Hub


The CoreMedia Studio Hub allows to integrate various external
and asset management systems into the Studio library and to preview items of these systems.
It allows you to integrate just about any external system or platform into your CoreMedia system.
The Studio Hub is implemented as a Blueprint extension.

### Documentation & Tutorial

https://github.com/CoreMedia/coremedia-studio-hub/tree/master/documentation

### Issue Tracker

https://github.com/CoreMedia/coremedia-studio-hub/issues

### Installation

Add our submodules to the extensions folder (cd modules/extensions): 

    git submodule add https://github.com/CoreMedia/coremedia-studio-hub.git

Add modules to modules/extensions/pom.xml

```<module>coremedia-feedback-hub</module>```

Execute the extension tool:

- workspace-config/extensions execute: mvn dependency:copy -Dartifact=com.coremedia.tools.extensions:extensions:LATEST:jar:all -DlocalRepositoryDirectory=extensions-tool -Dtransitive=false -DoutputDirectory=tool -Dmdep.stripVersion=true -Dmdep.stripClassifier=true
- workspace-config/extensions, execute: java -jar tool/extensions.jar --task synchronize --extension-config-file  extension-config.properties --task-input-file managed-extensions.txt

For the IDEA import:
- Ignore folder ".remote-package"
- Disable "Settings > Compiler > Clear output directory on rebuild"


