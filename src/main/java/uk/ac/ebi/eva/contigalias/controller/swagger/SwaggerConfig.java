/*
 * Copyright 2020 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.eva.contigalias.controller.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SwaggerConfig implements WebMvcConfigurer {

    private static final String CONTROLLER_BASE_PATH = "uk.ac.ebi.eva.contigalias.controller";

    @Autowired
    SwaggerInterceptAdapter interceptAdapter;

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("v1/contigalias")
                .packagesToScan(CONTROLLER_BASE_PATH + ".contigalias")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("v1/only-admins")
                .packagesToScan(CONTROLLER_BASE_PATH + ".admin")
                .build();
    }

    @Bean
    public OpenAPI contigAliasOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Contig-Alias API")
                        .description(
                                "Service to provide synonyms of chromosome/contig identifiers." +
                                "\nThe endpoints in the following controllers are paginated, which means that all " +
                                "results aren't returned at once, instead a small subset is returned. This small " +
                                "subset in the form of a page and the result need to be traversed through this set of" +
                                " pages." +
                                "\nYou can control pagination by specifying the index and size of the page you want " +
                                "using the two optional parameters \"page\" and \"size\" while querying the desired " +
                                "endpoint." +
                                "\nThe endpoints in the following controllers also provided hyperlinks to other " +
                                "relevant endpoints to help the user navigate the API with ease. These links are " +
                                "embedded inside an object called \"_links\" which is present at the root level of " +
                                "the response." +
                                "\nSome information about pagination is also similarly included in a root level " +
                                "object called \"page\". Due to this, the actual result is not available at the root " +
                                "level but is actually embedded in another root level element.")
                        .version("1.0")
                        .contact(new Contact()
                                .name("GitHub Repository")
                                .url("https://github.com/EBIvariation/contig-alias"))
                        .license(new License()
                                .name("Apache-2.0")
                                .url("https://raw.githubusercontent.com/EBIvariation/contig-alias/master/LICENSE")));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptAdapter);
    }
}
