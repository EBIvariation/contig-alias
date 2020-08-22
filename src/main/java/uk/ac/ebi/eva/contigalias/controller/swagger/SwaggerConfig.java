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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UriComponentsBuilder;
import springfox.documentation.PathProvider;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.paths.Paths;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfig implements WebMvcConfigurer {

    private final String CONTROLLER_BASE_PATH = "uk.ac.ebi.eva.contigalias.controller";

    @Autowired
    SwaggerInterceptAdapter interceptAdapter;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Bean
    public Docket publicApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("v1/contigalias")
                .pathProvider(getPathProvider())
                .apiInfo(getApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(CONTROLLER_BASE_PATH + ".contigalias"))
                .paths(PathSelectors.any())
                .build();
    }

    @Bean
    public Docket adminApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("v1/only-admins")
                .pathProvider(getPathProvider())
                .apiInfo(getApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(CONTROLLER_BASE_PATH + ".admin"))
                .paths(PathSelectors.any())
                .build();
    }

    private PathProvider getPathProvider() {
        return new PathProvider() {
            @Override
            public String getOperationPath(String operationPath) {
                if (operationPath.startsWith(contextPath)) {
                    operationPath = operationPath.substring(contextPath.length());
                }
                return Paths.removeAdjacentForwardSlashes(
                        UriComponentsBuilder.newInstance().replacePath(operationPath).build().toString());
            }

            @Override
            public String getResourceListingPath(String groupName, String apiDeclaration) {
                return null;
            }
        };
    }

    private ApiInfo getApiInfo() {
        return new ApiInfoBuilder()
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
                .contact(new Contact("GitHub Repository", "https://github.com/EBIvariation/contig-alias", null))
                .license("Apache-2.0")
                .licenseUrl("https://raw.githubusercontent.com/EBIvariation/contig-alias/master/LICENSE")
                .build();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptAdapter);
    }

}
