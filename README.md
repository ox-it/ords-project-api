# ords-project-api

The API for the Online Research Database Service (ORDS) project interface

## REST/JSON API

When the service is deployed, go to /api/1.0/project/swagger.json for REST documentation.

## Configuration Properties

    ords.mail.send = true

Determines whether to send out notification emails

    ords.mail.invitation.subject=Message from ORDS

The subject text to use for messages inviting a user to a project

    ords.mail.invitation.address=http://localhost/app/#/invite/%s

The verification URL to send the user. The %s token will be replaced with the invitation UUID.

    ords.mail.invitation.message=Hi!\n\n%s has suggested you join their ORDS project\, but you have not yet registered with the ORDS. Please click the following link to register with the ORDS and join their project.\n\n%s\n\nIf you believe this email has been sent to you in error then please contact ORDS support.\n\nThe ORDS Team

The invitation message body. The first %s token is replaced with user.name; the second with the invitation URL.

### Database and security configuration

    ords.hibernate.configuration=hibernate.cfg.xml

Optional; the location of the hibernate configuration file.

    ords.shiro.configuration=file:/etc/ords/shiro.ini

Optional; the location of the Shiro INI file

     ords.server.configuration=serverConfig.xml

Optional; the location of the Server configuration file

### Mail server configuration

The following properties are used for the email server connection

    mail.smtp.auth=true
    mail.smtp.starttls.enable=true
    mail.smtp.host=localhost
    mail.smtp.port=587
    mail.smtp.from=daemons@sysdev.oucs.ox.ac.uk
    mail.smtp.username=
    mail.smtp.password=


