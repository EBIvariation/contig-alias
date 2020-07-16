# contig-alias #
Reference sequences are files that are used as a reference to describe variants that are present in analyzed sequences and play a central role in defining a baseline of knowledge against which our understanding of biological systems, phenotypes and variation are based upon. Reference sequence files often use different naming schemes to refer to the same sequence and thus there is a strong need to be able to cross reference chromosomes/contigs using different nomenclatures. Thus there is a need for a centralized database with a alias resolution service that can cross reference accessions easily and reliably. Also a web service is required that allows users to access these services from any client and has a mechanism for manually or periodically ingesting new aliases from a remote datasource.


## Compile

This web service has some authenticated endpoints. The current approach to secure them is to provide the credentials in the src/main/resources/application.properties file at compilation time, using maven profiles.

The application also requires to be connected to an external Postgres database to function. The configuration parameters for this database need to be provided at compilation time using the same maven profiles. 

Copy this text, replace the values encolosed in ${} and put it all in your ~/.m2/settings.xml (or just add the profile if the file exists).
```
<settings>
    <profiles>
        <profile>
            <id>contig-alias</id>
            <properties>
                <contig-alias.admin-user>${your_user}</contig-alias.admin-user>
                <contig-alias.admin-password>${your_password}</contig-alias.admin-password>
                <contig-alias-dbUrl>jdbc:postgresql://${server_ip}:${db_port}/${db_name}</contig-alias-dbUrl>
                <contig-alias-dbUsername>${db_username}</contig-alias-dbUsername>
                <contig-alias-dbPassword>${db-password}</contig-alias-dbPassword>
                <contig-alias-ddlBehaviour>${preferred_behaviour}</contig-alias-ddlBehaviour>
            </properties>
        </profile>
    </profiles>
</settings>
```

Once that's done, you can trigger the variable replacement with the `-P` option in maven. Example: `mvn clean install -Pcontig-alias`.
 
