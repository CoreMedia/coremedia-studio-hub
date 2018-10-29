# Typeform Connector

This is a Studio Hub connector to integrate the Typform form service with CoreMedia Content Cloud.
It supports the following use cases:

- Browse the forms available in Typeform
- Create a reference with embed code in a HTML fragment in the content repository
- Download the form submissions from the Studio Hub

To use the connector you have to go through the following steps:

1. Setup Typeform account
2. Test account with connector
3. Install connector
4. Add connector to Blueprint workspace
5. Configure connector

The following chapters will guide you through the steps.

## Setup Typeform account

Go to [typeform](https://www.typeform.com) and register an account.
It is useful to have an text editor open as you will need to write down a few configurations.
Once you are logged in create a form and test it with a submission.
If you want to run the test in the next step you need at least one form with a submission.
Go to "Share" and copy the form id which is the last element form the Typeform link.

![Typeform Link](static/formId.png)

You will also need an embed code so that the form can be integrated with pages.

![Typeform Embed Code](static/embedCode.png)

Press "Get the code" and copy it to your editor.
Now go to "My Account - Applications" and create an application. You will need the "Client ID" later.

## Test account with connector

To be able to test the connector you need to add a profile to your `settings.xml` like this:

```
    <profile>
        <id>typeform-setup</id>
        <properties>
            <typeform.client.accesstoken><Client ID></typeform.client.accesstoken>
            <typeform.test>true</typeform.test>
            <typeform.test.form.id><Form ID></typeform.test.form.id>
        </properties>
    </profile>
```

Fill in <Client ID> and <Form ID> from the Typeform setup.

Now you can test the connector with `mvn test -P typeform-setup`. If everything is correct there will be no test failures.

## Install connector

With `mvn install -P typeform-setup` you now can install the connector into your local maven repository.

## Add connector to Blueprint workspace

To add the connector to the Blueprint workspace add the following dependency to the `studio-extension-dependencies` pom

```
    <dependency>
      <groupId>com.coremedia.labs.connectors</groupId>
      <artifactId>connectors-lib-typeform</artifactId>
      <version>1.0-SNAPSHOT</version>
      <scope>runtime</scope>
    </dependency>
```

## Configure connector

![Studio Library](static/studioLibrary.png)

Now you have to setup the connector in Studio. Open the Library and navigate to 
Connectors configuration `All Content/Settings/Options/Settings/Connectors/` where you
have to edit the following content items:

- Connector Types
- Connections
- Content Mapping

Add another type in "Connector Types"

```
<Struct>
    <StringProperty Name="name">Typeform</StringProperty>
    <StringProperty Name="connectorType">typeform</StringProperty>
    <StringProperty Name="defaultColumns">type,name</StringProperty>
    <LinkProperty Name="itemTypes" LinkType="coremedia:///cap/contenttype/CMSettings" xlink:href="coremedia:///cap/resources/Connector%20Item%20Types.xml" cmexport:path="/Settings/Options/Settings/Connectors/Connector%20Item%20Types"/>
    <LinkProperty Name="previewTemplates" LinkType="coremedia:///cap/contenttype/CMSettings" xlink:href="coremedia:///cap/resources/Preview%20Templates.xml" cmexport:path="/Settings/Options/Settings/Connectors/Preview%20Templates"/>
    <LinkProperty Name="contentMapping" LinkType="coremedia:///cap/contenttype/Content_" xlink:href="coremedia:///cap/resources/Content%20Mapping.xml" cmexport:path="/Settings/Options/Settings/Connectors/Content%20Mapping"/>
</Struct>
```

and make sure to set the connectorType to `typeform` as it is setup in the spring configutation `<bean id="connector:typeform" ...>`

![Connector Types](static/connectorTypes.png)

Next add a connection in "Connections"

```
<Struct>
    <StringProperty Name="displayName">Typeform</StringProperty>
    <StringProperty Name="connectionId">typeform1</StringProperty>
    <StringProperty Name="contentScope">site</StringProperty>
    <StringProperty Name="type">typeform</StringProperty>
    <StringProperty Name="accessToken">Client ID</StringProperty>
    <BooleanProperty Name="enabled">true</BooleanProperty>
    <StringProperty Name="embedCode">embed template code</StringProperty>
</Struct>
```

and set the `accessToken` to the "Client ID".
You also need the embed code which with a bit of formatting looks like this:

```
<div class="typeform-widget" data-url="https://xyz.typeform.com/to/XYZxyz" style="width: 100%; height: 500px;"></div> 
<script> 
    (function() { 
        var qs,js,q,s,d=document, gi=d.getElementById, ce=d.createElement, gt=d.getElementsByTagName, id="typef_orm", b="https://embed.typeform.com/"; 
        if(!gi.call(d,id)) { 
            js=ce.call(d,"script"); 
            js.id=id; js.src=b+"embed.js"; 
            q=gt.call(d,"script")[0]; 
            q.parentNode.insertBefore(js,q) 
        } 
    })() 
</script> 
<div style="font-family: Sans-Serif;font-size: 12px;color: #999;opacity: 0.5; padding-top: 5px;"> powered by 
    <a href="https://admin.typeform.com/signup?utm_campaign=MKRsh5&utm_source=typeform.com-12591912-Basic&utm_medium=typeform&utm_content=typeform-embedded-poweredbytypeform&utm_term=EN" style="color: #999" target="_blank">Typeform</a> 
</div>
```

The code here is formatted for readability it should not be formatted for the setup.
You can delete the last `<div>` which just adds a link to the service. 
As the connector uses the `String.format()` function some special characters must be encoded,
in this case the `%` must be replaced with `%%`. Finally you need to replase the data-url with
`data-url="%s" so that the code looks like this:

```
<div class="typeform-widget" data-url="%s" style="width: 100%%; height: 500px;"></div> 
<script> 
    (function() { 
        var qs,js,q,s,d=document, gi=d.getElementById, ce=d.createElement, gt=d.getElementsByTagName, id="typef_orm", b="https://embed.typeform.com/"; 
        if(!gi.call(d,id)) { 
            js=ce.call(d,"script"); 
            js.id=id; js.src=b+"embed.js"; 
            q=gt.call(d,"script")[0]; 
            q.parentNode.insertBefore(js,q) 
        } 
    })() 
</script> 
```
Now you can copy the code into the `embedCode` field.

![Connections](static/connections.png)

Finally you need to add the type `form` with the value `CMHTML` to "Content Mapping".

```
<StringProperty Name="form">CMHTML</StringProperty>
```

![Content Mapping](static/contentMapping.png)

Now reload the Studio and you should see a new Connector in the Library and can create
content items for forms.

## Test the integration

![Studio Connector](static/studioConnector.png)

Go to the Typeform connector and right click on the form you setup earlier. Now you can:

- Create new content item - And a new content item with the embed code. The preview will show the form and you can test it.
- Download - Downloads the submissions as CSV

## Future Improvements

- Preview template for Library
- Searching for forms