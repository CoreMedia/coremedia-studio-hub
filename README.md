# CoreMedia Studio Hub


The CoreMedia Studio Hub allows to integrate various external and asset 
management systems into the Studio library and to preview items of these 
systems. It allows you to integrate just about any external system or platform 
into your CoreMedia system. The Studio Hub is implemented as a Blueprint 
extension.

### Documentation & Tutorial

https://github.com/CoreMedia/coremedia-studio-hub/tree/master/documentation

### Issue Tracker

https://github.com/CoreMedia/coremedia-studio-hub/issues

### Installation

Add our submodules to the extensions folder

```
git submodule add https://github.com/CoreMedia/coremedia-studio-hub.git modules/extensions/connectors
```

Add modules to modules/extensions/pom.xml

```
<module>connectors</module>
```

Add extension to the list of managed extensions at the end of workspace-config/extensions/managed-extensions.txt:

```
echo "connectors" >> workspace-configuration/extensions/managed-extensions.txt
```

Download the extension tool:

```
mvn dependency:copy -Dartifact=com.coremedia.tools.extensions:extensions:LATEST:jar:all -DlocalRepositoryDirectory=extensions-tool -Dtransitive=false -DoutputDirectory=tool -Dmdep.stripVersion=true -Dmdep.stripClassifier=true
```

Execute the extension tool:

```
java -jar tool/extensions.jar --task synchronize --extension-config-file  workspace-configuration/extensions/extension-config.properties --task-input-file workspace-configuration/extensions/managed-extensions.txt
```

For the IDEA import:
- Ignore folder ".remote-package"
- Disable "Settings > Compiler > Clear output directory on rebuild"
