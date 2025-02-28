/**
 * Copyright 2016 SmartBear Software, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smartbear.ready.plugin.postman;

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.graphql.GraphQLRequest;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.support.wsdl.UrlClientLoader;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.GraphQLTestRequestTestStepWithSchema;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.support.ModelItemNamer;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;
import com.smartbear.ready.plugin.postman.collection.RequestAuthProfile;
import com.smartbear.ready.plugin.postman.collection.authorization.AuthorizationProfileImporter;
import com.smartbear.ready.plugin.postman.collection.PostmanCollection;
import com.smartbear.ready.plugin.postman.collection.PostmanCollectionFactory;
import com.smartbear.ready.plugin.postman.collection.Request;
import com.smartbear.ready.plugin.postman.exceptions.PostmanCollectionUnsupportedVersionException;
import com.smartbear.ready.plugin.postman.script.PostmanScriptParserV1;
import com.smartbear.ready.plugin.postman.script.PostmanScriptParserV2;
import com.smartbear.ready.plugin.postman.script.PostmanScriptTokenizer;
import com.smartbear.ready.plugin.postman.script.PostmanScriptTokenizer.Token;
import com.smartbear.ready.plugin.postman.script.ScriptContext;
import com.smartbear.ready.plugin.postman.utils.GraphQLImporterUtils;
import com.smartbear.ready.plugin.postman.utils.PostmanJsonUtil;
import com.smartbear.ready.plugin.postman.utils.RestServiceCreator;
import com.smartbear.ready.plugin.postman.utils.SoapServiceCreator;
import com.smartbear.ready.plugin.postman.utils.VaultVariableResolver;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.smartbear.ready.plugin.postman.utils.GraphQLImporterUtils.isGraphQlRequest;

public class PostmanImporter {

    private static final Logger logger = LoggerFactory.getLogger(PostmanImporter.class);
    private static String foldersAmount;
    private static String requestsAmount;
    private final TestCreator testCreator;
    private final VaultVariableResolver resolver;

    public PostmanImporter(TestCreator testCreator) {
        this.testCreator = testCreator;
        this.resolver = new VaultVariableResolver();
    }

    public PostmanImporter(TestCreator testCreator, VaultVariableResolver resolver) {
        this.testCreator = testCreator;
        this.resolver = resolver;
    }

    public WsdlProject importPostmanCollection(WorkspaceImpl workspace, String filePath) throws PostmanCollectionUnsupportedVersionException {
        WsdlProject project = null;
        String postmanJson = getPostmanImporterWorker(filePath).getPostmanJson();
        if (PostmanJsonUtil.seemsToBeJson(postmanJson)) {
            JSON json = new PostmanJsonUtil().parseTrimmedText(postmanJson);
            if (json instanceof JSONObject jsonCollection) {
                PostmanCollection postmanCollection = PostmanCollectionFactory.getCollection(jsonCollection);
                String collectionName = postmanCollection.getName();
                foldersAmount = Integer.toString(postmanCollection.getFolders().size());
                requestsAmount = Integer.toString(postmanCollection.getRequests().size());
                String projectName = createProjectName(collectionName, workspace.getProjectList());
                try {
                    project = workspace.createProject(projectName, null);
                } catch (SoapUIException e) {
                    logger.error("Error while creating a project", e);
                    return null;
                }
                project.setDescription(postmanCollection.getDescription());

                AuthorizationProfileImporter authProfileImporter = new AuthorizationProfileImporter(postmanCollection.getVersion(), project);
                authProfileImporter.importAuthorizationProfile(postmanCollection.getAuth(), postmanCollection.getName(), project);

                List<Request> requests = postmanCollection.getRequests();
                for (Request request : requests) {
                    String uri = request.getUrl();
                    String preRequestScript = request.getPreRequestScript();
                    String tests = request.getTests();
                    String requestName = request.getName();
                    RequestAuthProfile authProfile = request.getAuthProfileWithName();

                    if (StringUtils.hasContent(preRequestScript)) {
                        processPreRequestScript(preRequestScript, project);
                    }

                    Assertable assertable = null;

                    if (request.isSoap()) {
                        logger.info("Importing a SOAP request with URI [ {} ] - started", uri);

                        SoapServiceCreator soapServiceCreator = new SoapServiceCreator(project);
                        WsdlRequest wsdlRequest = soapServiceCreator.addSoapRequest(request);

                        authProfileImporter.importAuthorizationProfile(authProfile.getAuthProfile(), authProfile.getProfileName(), wsdlRequest);

                        if (StringUtils.hasContent(tests)) {
                            testCreator.createTest(wsdlRequest, collectionName);
                            assertable = getTestRequestStep(project, WsdlTestRequestStep.class);
                        }
                    } else if (isGraphQlRequest(request)) {
                        logger.info("Importing a GraphQL request with URI [ {} ] - started", uri);

                        GraphQLImporterUtils graphQLImporterUtils = new GraphQLImporterUtils();
                        GraphQLRequest graphQLRequest = graphQLImporterUtils.addGraphQLRequest(project, request);
                        if (graphQLRequest == null) {
                            logger.error("Could not import {} request with URI [ {} ]", request.getMethod(), uri);
                            continue;
                        }
                        authProfileImporter.importAuthorizationProfile(authProfile.getAuthProfile(), authProfile.getProfileName(), graphQLRequest);

                        if (StringUtils.hasContent(tests)) {
                            testCreator.createTest(graphQLRequest, collectionName);
                            assertable = getTestRequestStep(project, GraphQLTestRequestTestStepWithSchema.class);
                        }
                    } else {
                        logger.info("Importing a REST request with URI [ {} ] - started", uri);
                        RestRequest restRequest = new RestServiceCreator(project).addRestRequest(request);
                        if (restRequest == null) {
                            logger.error("Could not import {} request with URI [ {} ]", request.getMethod(), uri);
                            continue;
                        }
                        authProfileImporter.importAuthorizationProfile(authProfile.getAuthProfile(), authProfile.getProfileName(), restRequest);

                        if (StringUtils.hasContent(tests)) {
                            testCreator.createTest(restRequest, collectionName);
                            assertable = getTestRequestStep(project, RestTestRequestStep.class);
                        }
                    }

                    if (assertable != null) {
                        addAssertionsV2(tests, project, assertable, requestName);
                    }

                    logger.info("Importing a request with URI [ {} ] - done", uri);
                }
                handleVaultVariables(jsonCollection, project);

                List<PostmanCollection.Variable> variables = postmanCollection.getVariables();
                if (variables != null) {
                    for (PostmanCollection.Variable variable : variables) {
                        String propertyName = variable.getKey();
                        if (!project.hasProperty(propertyName)) {
                            project.addProperty(propertyName);
                        }

                        project.setPropertyValue(propertyName, variable.getValue());
                    }
                }
            }
        }
        return project;
    }

    private void handleVaultVariables(JSONObject jsonCollection, WsdlProject project) {
        Map<String,String> vaultVariables = resolver.resolve(jsonCollection);
        for (Map.Entry<String, String> vaultVariable : vaultVariables.entrySet()) {
            if (!project.hasProperty(vaultVariable.getKey())) {
                project.addProperty(vaultVariable.getKey());
            }
            project.setPropertyValue(vaultVariable.getKey(), vaultVariable.getValue());
        }
    }

    private PostmanImporterWorker getPostmanImporterWorker(String filePath) {
        XProgressDialog collectionImportProgressDialog = UISupport.getDialogs().createProgressDialog("Import Collection",
                0, "Importing the collection...", false);
        PostmanImporterWorker worker = new PostmanImporterWorker(filePath);
        try {
            collectionImportProgressDialog.run(worker);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return worker;
    }

    private static String createProjectName(String collectionName, List<? extends Project> projectList) {
        Class clazz;
        try {
            clazz = Class.forName("com.eviware.soapui.support.ModelItemNamer$NumberSuffixStrategy");
            Method method = ModelItemNamer.class.getMethod("createName", String.class, Iterable.class, clazz);
            if (clazz.isEnum()) {
                return (String) method.invoke(null, collectionName, projectList,
                        Enum.valueOf(clazz, "SUFFIX_WHEN_CONFLICT_FOUND"));
            }
        } catch (Throwable e) {
            logger.warn("Setting number suffix strategy is only supported in ReadyAPI", e);
        }

        return ModelItemNamer.createName(collectionName, projectList);
    }

    private void addHttpHeaders(AbstractHttpRequest request, List<PostmanCollection.Header> headers,
                                WsdlProject projectToAddProperties) {
        for (PostmanCollection.Header header : headers) {
            StringToStringsMap headersMap = request.getRequestHeaders();
            headersMap.add(header.getKey(), VariableUtils.convertVariables(header.getValue(), projectToAddProperties));
            request.setRequestHeaders(headersMap);
        }
    }


    private void addAssertionsV1(String tests, WsdlProject project, Assertable assertable) {
        PostmanScriptTokenizer tokenizer = new PostmanScriptTokenizer();
        PostmanScriptParserV1 parser = new PostmanScriptParserV1();
        try {
            LinkedList<Token> tokens = tokenizer.tokenize(tests);

            ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);
            parser.parse(tokens, context);
        } catch (SoapUIException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void addAssertionsV2(String tests, WsdlProject project, Assertable assertable, String requestName) {
        ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);
        PostmanScriptParserV2 parserV2 = new PostmanScriptParserV2(context);
        parserV2.parse(tests, requestName);

        if (StringUtils.hasContent(parserV2.getTestsV1())) {
            addAssertionsV1(parserV2.getTestsV1(), project, assertable);
        }
    }

    private void processPreRequestScript(String preRequestScript, WsdlProject project) {
        ScriptContext context = ScriptContext.preparePreRequestScriptContext(project);
        PostmanScriptParserV2 parserV2 = new PostmanScriptParserV2(context);
        parserV2.findAndAddSettingGlobalVariables(preRequestScript);

        if (StringUtils.hasContent(parserV2.getPrescriptV1())) {
            PostmanScriptTokenizer tokenizer = new PostmanScriptTokenizer();
            PostmanScriptParserV1 parser = new PostmanScriptParserV1();
            try {
                LinkedList<Token> tokens = tokenizer.tokenize(parserV2.getPrescriptV1());
                parser.parse(tokens, context);
            } catch (SoapUIException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private <T> T getTestRequestStep(WsdlProject project, Class<T> stepClass) {
        if (project.getTestSuiteCount() > 0) {
            WsdlTestSuite testSuite = project.getTestSuiteAt(project.getTestSuiteCount() - 1);
            if (testSuite != null && testSuite.getTestCaseCount() > 0) {
                WsdlTestCase testCase = testSuite.getTestCaseAt(testSuite.getTestCaseCount() - 1);
                if (testCase != null && testCase.getTestStepCount() > 0) {
                    WsdlTestStep testStep = testCase.getTestStepAt(testCase.getTestStepCount() - 1);
                    if (stepClass.isInstance(testStep)) {
                        return (T) testStep;
                    }
                }
            }
        }
        return null;
    }

    /**
     * https://smartbear.atlassian.net/wiki/spaces/PD/pages/172544951/ReadyAPI+analytics+home-phone+data+revision
     */
    public static void sendAnalytics(int testStepsAmount) {
        Class analyticsClass;
        try {
            analyticsClass = Class.forName("com.smartbear.analytics.Analytics");
        } catch (ClassNotFoundException e) {
            return;
        }
        try {
            Method getManagerMethod = analyticsClass.getMethod("getAnalyticsManager");
            Object analyticsManager = getManagerMethod.invoke(null);
            Class analyticsCategoryClass = Class.forName("com.smartbear.analytics.AnalyticsManager$Category");
            Method trackMethod = analyticsManager.getClass().getMethod("trackAction", analyticsCategoryClass,
                    String.class, Map.class);
            Map<String, String> paramsForCreateProject = new HashMap();
            paramsForCreateProject.put("SourceModule", "Any");
            paramsForCreateProject.put("ProductArea", "MainMenu");
            paramsForCreateProject.put("Type", "REST");
            paramsForCreateProject.put("Source", "PostmanCollection");
            trackMethod.invoke(analyticsManager, Enum.valueOf(analyticsCategoryClass, "CUSTOM_PLUGIN_ACTION"),
                    "CreateProject", paramsForCreateProject);

            Map<String, String> paramsForImportPostmnaCollection = new HashMap();
            paramsForImportPostmnaCollection.put("NumberOfStepsCreated", Integer.toString(testStepsAmount));
            paramsForImportPostmnaCollection.put("NumberOfFolders", foldersAmount);
            paramsForImportPostmnaCollection.put("NumberOfRequests", requestsAmount);
            trackMethod.invoke(analyticsManager, Enum.valueOf(analyticsCategoryClass, "CUSTOM_PLUGIN_ACTION"),
                    "ImportPostmanCollection", paramsForImportPostmnaCollection);
        } catch (Throwable e) {
            logger.error("Error while sending analytics", e);
        }
    }

    private class PostmanImporterWorker implements Worker {
        private String url;
        private String postmanJson;

        public PostmanImporterWorker(String url) {
            this.url = url;
        }

        @Override
        public Object construct(XProgressMonitor monitor) {
            UrlClientLoader loader = new UrlClientLoader(url);
            try {
                loader.setUseWorker(false);
                postmanJson = IOUtils.toString(loader.load(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                UISupport.showErrorMessage(e.getMessage());
            }
            return postmanJson;
        }

        @Override
        public void finished() {

        }

        @Override
        public boolean onCancel() {
            return false;
        }

        public String getPostmanJson() {
            return postmanJson;
        }
    }
}
