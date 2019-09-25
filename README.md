![Status: Active](https://documentation.coremedia.com/badges/badge_status_active.png "Status: Active")
![For CoreMedia CMS](https://documentation.coremedia.com/badges/badge_coremedia_cms.png "For CoreMedia CMS")

![CoreMedia Labs Logo](https://documentation.coremedia.com/badges/banner_coremedia_labs_wide.png "CoreMedia Labs Logo Title Text")


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
 
- Clone this repository as submodule into the extensions folder. 
```
[PROJECT_ROOT]>git submodule add https://github.com/CoreMedia/coremedia-studio-hub modules/extensions/coremedia-studio-hub
```

- Change to the submodule folder.
```
[PROJECT_ROOT]>cd modules/extensions/coremedia-studio-hub
```

- Checkout the branch that matches your workspace version.
```
[PROJECT_ROOT]/modules/extensions/coremedia-studio-hub>git checkout 1907.1
```

- Link the project into your workspace using the extension tool.
 ```
[PROJECT_ROOT]>mvn -f workspace-configuration/extensions com.coremedia.maven:extensions-maven-plugin:LATEST:sync -Denable=coremedia-studio-hub
```

- Rebuild the workspace

For CI runs:
- Ensure that the matching branch name is set in the _.gitmodules_ file, e.g.:

```
[submodule "modules/extensions/coremedia-studio-hub"]
	path = modules/extensions/coremedia-studio-hub
	url = https://github.com/CoreMedia/coremedia-studio-hub.git
	branch = 1907.1
```

For the IDEA import:
- Ignore folder _.remote-package_
- Disable "Settings > Compiler > Clear output directory on rebuild"
