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

- Create the new top level folders in your workspace: _/modules/extensions/_
- Clone this repository as submodule into the extensions folder. (/modules/extensions>_git submodule add https://github.com/CoreMedia/coremedia-studio-hub_)
- Checkout the branch that matches your workspace version, e.g.: _git checkout 1907.1_
- Link the project into your workspace using the extension tool: _mvn -f workspace-configuration/extensions com.coremedia.maven:extensions-maven-plugin:LATEST:sync -Denable=coremedia-studio-hub_
- Rebuild the workspace

For the IDEA import:
- Ignore folder ".remote-package"
- Disable "Settings > Compiler > Clear output directory on rebuild"
