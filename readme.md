# contig-alias #
Reference sequences are files that are used as a reference to describe variants that are present in analyzed sequences and play a central role in defining a baseline of knowledge against which our understanding of biological systems, phenotypes and variation are based upon. Reference sequence files often use different naming schemes to refer to the same sequence and thus there is a strong need to be able to cross reference chromosomes/contigs using different nomenclatures. Thus there is a need for a centralized database with a alias resolution service that can cross reference accessions easily and reliably. Also a web service is required that allows users to access these services from any client and has a mechanism for manually or periodically ingesting new aliases from a remote datasource.


## Compile

This web service has some authenticated endpoints. The current approach to secure them is to provide the credentials in the src/main/resources/application.properties file at compilation time, using maven profiles.

The application also requires to be connected to an external database (PostgreSQL by default) to function. The credentials for this database need to be provided at compilation time using the same maven profiles. 

Copy this text, replace manually the values enclosed in ${} and put it all in your ~/.m2/settings.xml (or just add the profile if the file exists).

Use `<ftp.proxy.host>` and `<ftp.proxy.port>` to configure proxy settings for accessing FTP servers (such as NCBI's). Set them to `null` and `0` to prevent overriding default the proxy configuration.

Set a boolean flag using `<contig-alias.scaffolds-enabled>` to enable or disable parsing and storing of scaffolds in the database.
```
<settings>
    <profiles>
        <profile>
            <id>contig-alias</id>
            <properties>
                <contig-alias.admin-user>${your_user}</contig-alias.admin-user>
                <contig-alias.admin-password>${your_password}</contig-alias.admin-password>
                <contig-alias.db-url>jdbc:postgresql://${server_ip}:${db_port}/${db_name}</contig-alias.db-url>
                <contig-alias.db-username>${db_username}</contig-alias.db-username>
                <contig-alias.db-password>${db-password}</contig-alias.db-password>
                <contig-alias.ddl-behaviour>${preferred_behaviour}</contig-alias.ddl-behaviour>
                <ftp.proxy.host>${optional default=null}</ftp.proxy.host>
                <ftp.proxy.port>${optional default=0}</ftp.proxy.port>
                <contig-alias.scaffolds-enabled>${optional default=false}</contig-alias.scaffolds-enabled>
            </properties>
        </profile>
    </profiles>
</settings>
```

Once that's done, you can trigger the variable replacement with the `-P` option in maven. Example: `mvn clean install -Pcontig-alias`.
