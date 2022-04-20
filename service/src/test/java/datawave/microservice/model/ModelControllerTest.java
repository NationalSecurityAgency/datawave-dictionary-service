package datawave.microservice.model;

import datawave.accumulo.inmemory.InMemoryInstance;
import datawave.microservice.ControllerIT;
import datawave.microservice.model.config.ModelProperties;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

public class ModelControllerTest extends ControllerIT {
    
    @Autowired
    private ModelProperties modelProperties;
    
    @ComponentScan(basePackages = "datawave.microservice")
    @Configuration
    public static class DataDictionaryImplTestConfiguration {
        @Bean
        @Qualifier("warehouse")
        public Connector warehouseConnector() throws AccumuloSecurityException, AccumuloException {
            return new InMemoryInstance().getConnector("root", new PasswordToken(""));
        }
    }
    
    @BeforeEach
    public void setUp() throws Exception {
        try {
            connector.tableOperations().create(modelProperties.getDefaultTableName());
        } catch (TableExistsException e) {
            // ignore
        }
    };
    
    // private static final String userDN = "CN=Guy Some Other soguy, OU=ou1, OU=ou2, OU=ou3, O=o1, C=US";
    // private static final String issuerDN = "CN=CA1, OU=ou3, O=o1, C=US";
    // private static final String[] auths = new String[] {"PRIVATE", "PUBLIC"};
    //
    // private ModelController modelController;
    // private AccumuloConnectionService connection;
    //// private AccumuloTableCache cache;
    //
    // private InMemoryInstance instance = null;
    // private Connector connector = null;
    //// private DatawavePrincipal principal = null;
    //
    // private static long TIMESTAMP = System.currentTimeMillis();
    //
    //// private datawave.webservice.model.Model MODEL_ONE = null;
    //// private datawave.webservice.model.Model MODEL_TWO = null;
    //
    // @BeforeEach
    // public void setup() throws Exception {
    //// System.setProperty(NpeUtils.NPE_OU_PROPERTY, "iamnotaperson");
    // System.setProperty("dw.metadatahelper.all.auths", "A,B,C,D");
    // bean = new ModelBean();
    // connectionFactory = createStrictMock(AccumuloConnectionFactory.class);
    // ctx = createMock(EJBContext.class);
    // cache = createMock(AccumuloTableCache.class);
    // Whitebox.setInternalState(bean, EJBContext.class, ctx);
    // Whitebox.setInternalState(bean, AccumuloConnectionFactory.class, connectionFactory);
    // Whitebox.setInternalState(bean, AccumuloTableCache.class, cache);
    //
    // instance = new InMemoryInstance("test");
    // connector = instance.getConnector("root", new PasswordToken(""));
    //
    // DatawaveUser user = new DatawaveUser(SubjectIssuerDNPair.of(userDN, issuerDN), UserType.USER, Arrays.asList(auths), null, null, 0L);
    // principal = new DatawavePrincipal(Collections.singletonList(user));
    //
    // URL m1Url = ModelBeanTest.class.getResource("/ModelBeanTest_m1.xml");
    // URL m2Url = ModelBeanTest.class.getResource("/ModelBeanTest_m2.xml");
    // JAXBContext ctx = JAXBContext.newInstance(datawave.webservice.model.Model.class);
    // Unmarshaller u = ctx.createUnmarshaller();
    // MODEL_ONE = (datawave.webservice.model.Model) u.unmarshal(m1Url);
    // MODEL_TWO = (datawave.webservice.model.Model) u.unmarshal(m2Url);
    //
    // Logger.getLogger(ModelBean.class).setLevel(Level.OFF);
    // PowerMock.mockStatic(System.class, System.class.getMethod("currentTimeMillis"));
    // }
    //
    // public void printTable(String tableName) throws Exception {
    // Scanner s = connector.createScanner(tableName, new Authorizations(auths));
    // for (Entry<Key,Value> entry : s) {
    // System.out.println(entry.getKey());
    // }
    // }
    //
    // @After
    // public void tearDown() {
    // try {
    // connector.tableOperations().delete(ModelBean.DEFAULT_MODEL_TABLE_NAME);
    // } catch (Exception e) {}
    // }
    //
    //// @Test(expected = DatawaveWebApplicationException.class)
    //// public void testModelImportNoTable() throws Exception {
    //// HashMap<String,String> trackingMap = new HashMap<>();
    //// EasyMock.expect(connectionFactory.getTrackingMap((StackTraceElement[]) EasyMock.anyObject())).andReturn(trackingMap);
    //// EasyMock.expect(connectionFactory.getConnection(EasyMock.eq(AccumuloConnectionFactory.Priority.LOW), EasyMock.eq(trackingMap))).andReturn(connector);
    //// connectionFactory.returnConnection(connector);
    //// EasyMock.expect(ctx.getCallerPrincipal()).andReturn(principal);
    //// PowerMock.replayAll();
    ////
    //// bean.importModel(MODEL_ONE, (String) null);
    //// PowerMock.verifyAll();
    //// }
    ////
    // private void importModels() throws Exception {
    // connector.tableOperations().create(ModelBean.DEFAULT_MODEL_TABLE_NAME);
    //
    // HashMap<String,String> trackingMap = new HashMap<>();
    // EasyMock.expect(connectionFactory.getTrackingMap((StackTraceElement[]) EasyMock.anyObject())).andReturn(trackingMap);
    // EasyMock.expect(connectionFactory.getConnection(EasyMock.eq(AccumuloConnectionFactory.Priority.LOW), EasyMock.eq(trackingMap))).andReturn(connector);
    // connectionFactory.returnConnection(connector);
    // EasyMock.expect(ctx.getCallerPrincipal()).andReturn(principal);
    // EasyMock.expect(connectionFactory.getTrackingMap((StackTraceElement[]) EasyMock.anyObject())).andReturn(trackingMap);
    // EasyMock.expect(connectionFactory.getConnection(EasyMock.eq(AccumuloConnectionFactory.Priority.LOW), EasyMock.eq(trackingMap))).andReturn(connector);
    // EasyMock.expect(System.currentTimeMillis()).andReturn(TIMESTAMP);
    // connectionFactory.returnConnection(connector);
    // EasyMock.expect(System.currentTimeMillis()).andReturn(TIMESTAMP);
    // EasyMock.expect(System.currentTimeMillis()).andReturn(TIMESTAMP);
    // EasyMock.expect(cache.reloadCache(ModelBean.DEFAULT_MODEL_TABLE_NAME)).andReturn(null);
    // PowerMock.replayAll();
    //
    // bean.importModel(MODEL_ONE, (String) null);
    // PowerMock.verifyAll();
    // PowerMock.resetAll();
    //
    // EasyMock.expect(connectionFactory.getTrackingMap((StackTraceElement[]) EasyMock.anyObject())).andReturn(trackingMap);
    // EasyMock.expect(connectionFactory.getConnection(EasyMock.eq(AccumuloConnectionFactory.Priority.LOW), EasyMock.eq(trackingMap))).andReturn(connector);
    // connectionFactory.returnConnection(connector);
    // EasyMock.expect(ctx.getCallerPrincipal()).andReturn(principal);
    // EasyMock.expect(connectionFactory.getTrackingMap((StackTraceElement[]) EasyMock.anyObject())).andReturn(trackingMap);
    // EasyMock.expect(connectionFactory.getConnection(EasyMock.eq(AccumuloConnectionFactory.Priority.LOW), EasyMock.eq(trackingMap))).andReturn(connector);
    // EasyMock.expect(System.currentTimeMillis()).andReturn(TIMESTAMP);
    // connectionFactory.returnConnection(connector);
    // EasyMock.expect(System.currentTimeMillis()).andReturn(TIMESTAMP);
    // EasyMock.expect(System.currentTimeMillis()).andReturn(TIMESTAMP);
    // EasyMock.expect(System.currentTimeMillis()).andReturn(TIMESTAMP);
    // EasyMock.expect(cache.reloadCache(ModelBean.DEFAULT_MODEL_TABLE_NAME)).andReturn(null);
    // PowerMock.replayAll();
    //
    // bean.importModel(MODEL_TWO, (String) null);
    //
    // PowerMock.verifyAll();
    // }
    //
    // @Test
    // public void testListModels() throws Exception {
    // importModels();
    // PowerMock.resetAll();
    //
    // EasyMock.expect(ctx.getCallerPrincipal()).andReturn(principal);
    // HashMap<String,String> trackingMap = new HashMap<>();
    // EasyMock.expect(connectionFactory.getTrackingMap((StackTraceElement[]) EasyMock.anyObject())).andReturn(trackingMap);
    // EasyMock.expect(connectionFactory.getConnection(EasyMock.eq(AccumuloConnectionFactory.Priority.LOW), EasyMock.eq(trackingMap))).andReturn(connector);
    // connectionFactory.returnConnection(connector);
    // PowerMock.replayAll();
    //
    // ModelList list = bean.listModelNames((String) null);
    // PowerMock.verifyAll();
    //
    // Assert.assertEquals(2, list.getNames().size());
    // Assert.assertTrue(list.getNames().contains(MODEL_ONE.getName()));
    // Assert.assertTrue(list.getNames().contains(MODEL_TWO.getName()));
    // }
    //
    // @Test
    // public void testModelGet() throws Exception {
    // importModels();
    // PowerMock.resetAll();
    //
    // EasyMock.expect(ctx.getCallerPrincipal()).andReturn(principal);
    // HashMap<String,String> trackingMap = new HashMap<>();
    // EasyMock.expect(connectionFactory.getTrackingMap((StackTraceElement[]) EasyMock.anyObject())).andReturn(trackingMap);
    // EasyMock.expect(connectionFactory.getConnection(EasyMock.eq(AccumuloConnectionFactory.Priority.LOW), EasyMock.eq(trackingMap))).andReturn(connector);
    // connectionFactory.returnConnection(connector);
    // PowerMock.replayAll();
    //
    // datawave.webservice.model.Model model = bean.getModel(MODEL_ONE.getName(), (String) null);
    // PowerMock.verifyAll();
    //
    // Assert.assertEquals(MODEL_ONE, model);
    // }
    //
    // @Test
    // public void testModelDelete() throws Exception {
    // importModels();
    // PowerMock.resetAll();
    //
    // EasyMock.expect(ctx.getCallerPrincipal()).andReturn(principal);
    // HashMap<String,String> trackingMap = new HashMap<>();
    // EasyMock.expect(connectionFactory.getTrackingMap((StackTraceElement[]) EasyMock.anyObject())).andReturn(trackingMap);
    // EasyMock.expect(connectionFactory.getConnection(EasyMock.eq(AccumuloConnectionFactory.Priority.LOW), EasyMock.eq(trackingMap))).andReturn(connector);
    // connectionFactory.returnConnection(connector);
    // EasyMock.expect(connectionFactory.getTrackingMap((StackTraceElement[]) EasyMock.anyObject())).andReturn(trackingMap);
    // EasyMock.expect(connectionFactory.getConnection(EasyMock.eq(AccumuloConnectionFactory.Priority.LOW), EasyMock.eq(trackingMap))).andReturn(connector);
    // connectionFactory.returnConnection(connector);
    // EasyMock.expect(ctx.getCallerPrincipal()).andReturn(principal);
    // EasyMock.expect(connectionFactory.getTrackingMap((StackTraceElement[]) EasyMock.anyObject())).andReturn(trackingMap);
    // EasyMock.expect(connectionFactory.getConnection(EasyMock.eq(AccumuloConnectionFactory.Priority.LOW), EasyMock.eq(trackingMap))).andReturn(connector);
    // EasyMock.expect(System.currentTimeMillis()).andReturn(TIMESTAMP);
    // EasyMock.expect(System.currentTimeMillis()).andReturn(TIMESTAMP);
    // EasyMock.expect(System.currentTimeMillis()).andReturn(TIMESTAMP);
    // connectionFactory.returnConnection(connector);
    // EasyMock.expect(cache.reloadCache(ModelBean.DEFAULT_MODEL_TABLE_NAME)).andReturn(null);
    // EasyMock.expect(System.currentTimeMillis()).andReturn(TIMESTAMP);
    // EasyMock.expect(System.currentTimeMillis()).andReturn(TIMESTAMP);
    // EasyMock.expect(System.currentTimeMillis()).andReturn(TIMESTAMP);
    // PowerMock.replayAll();
    //
    // bean.deleteModel(MODEL_TWO.getName(), (String) null);
    // PowerMock.verifyAll();
    // PowerMock.resetAll();
    //
    // EasyMock.expect(ctx.getCallerPrincipal()).andReturn(principal);
    // EasyMock.expect(connectionFactory.getTrackingMap((StackTraceElement[]) EasyMock.anyObject())).andReturn(trackingMap);
    // EasyMock.expect(connectionFactory.getConnection(EasyMock.eq(AccumuloConnectionFactory.Priority.LOW), EasyMock.eq(trackingMap))).andReturn(connector);
    // connectionFactory.returnConnection(connector);
    // PowerMock.replayAll();
    // try {
    // bean.getModel(MODEL_TWO.getName(), (String) null);
    // Assert.fail("getModel should have failed");
    // } catch (DatawaveWebApplicationException e) {
    // if (e.getResponse().getStatus() == 404) {
    // // success
    // } else {
    // Assert.fail("getModel did not return a 404, returned: " + e.getResponse().getStatus());
    // }
    // } catch (Exception ex) {
    // Assert.fail("getModel did not throw a DatawaveWebApplicationException");
    // }
    // PowerMock.verifyAll();
    // PowerMock.resetAll();
    // // Ensure model one still intact
    // EasyMock.expect(ctx.getCallerPrincipal()).andReturn(principal);
    // EasyMock.expect(connectionFactory.getTrackingMap((StackTraceElement[]) EasyMock.anyObject())).andReturn(trackingMap);
    // EasyMock.expect(connectionFactory.getConnection(EasyMock.eq(AccumuloConnectionFactory.Priority.LOW), EasyMock.eq(trackingMap))).andReturn(connector);
    // connectionFactory.returnConnection(connector);
    // PowerMock.replayAll();
    // datawave.webservice.model.Model model1 = bean.getModel(MODEL_ONE.getName(), (String) null);
    // PowerMock.verifyAll();
    // Assert.assertEquals(MODEL_ONE, model1);
    //
    // }
    //
    // @Test(expected = DatawaveWebApplicationException.class)
    // public void testModelGetInvalidModelName() throws Exception {
    // EasyMock.expect(ctx.getCallerPrincipal()).andReturn(principal);
    // HashMap<String,String> trackingMap = new HashMap<>();
    // EasyMock.expect(connectionFactory.getTrackingMap((StackTraceElement[]) EasyMock.anyObject())).andReturn(trackingMap);
    // EasyMock.expect(connectionFactory.getConnection(EasyMock.eq(AccumuloConnectionFactory.Priority.LOW), EasyMock.eq(trackingMap))).andReturn(connector);
    // connectionFactory.returnConnection(connector);
    // PowerMock.replayAll();
    //
    // bean.getModel(MODEL_ONE.getName(), (String) null);
    // PowerMock.verifyAll();
    // }
    //
    // @Test
    // public void testCloneModel() throws Exception {
    // importModels();
    // PowerMock.resetAll();
    //
    // EasyMock.expect(ctx.getCallerPrincipal()).andReturn(principal);
    // HashMap<String,String> trackingMap = new HashMap<>();
    // EasyMock.expect(connectionFactory.getTrackingMap((StackTraceElement[]) EasyMock.anyObject())).andReturn(trackingMap);
    // EasyMock.expect(connectionFactory.getConnection(EasyMock.eq(AccumuloConnectionFactory.Priority.LOW), EasyMock.eq(trackingMap))).andReturn(connector);
    // connectionFactory.returnConnection(connector);
    // EasyMock.expect(connectionFactory.getTrackingMap((StackTraceElement[]) EasyMock.anyObject())).andReturn(trackingMap);
    // EasyMock.expect(connectionFactory.getConnection(EasyMock.eq(AccumuloConnectionFactory.Priority.LOW), EasyMock.eq(trackingMap))).andReturn(connector);
    // connectionFactory.returnConnection(connector);
    // EasyMock.expect(ctx.getCallerPrincipal()).andReturn(principal);
    // EasyMock.expect(connectionFactory.getTrackingMap((StackTraceElement[]) EasyMock.anyObject())).andReturn(trackingMap);
    // EasyMock.expect(connectionFactory.getConnection(EasyMock.eq(AccumuloConnectionFactory.Priority.LOW), EasyMock.eq(trackingMap))).andReturn(connector);
    // EasyMock.expect(cache.reloadCache(ModelBean.DEFAULT_MODEL_TABLE_NAME)).andReturn(null);
    // EasyMock.expect(System.currentTimeMillis()).andReturn(TIMESTAMP);
    // connectionFactory.returnConnection(connector);
    // EasyMock.expect(System.currentTimeMillis()).andReturn(TIMESTAMP);
    // EasyMock.expect(System.currentTimeMillis()).andReturn(TIMESTAMP);
    // PowerMock.replayAll();
    //
    // bean.cloneModel(MODEL_ONE.getName(), "MODEL2", (String) null);
    // PowerMock.verifyAll();
    // PowerMock.resetAll();
    // EasyMock.expect(ctx.getCallerPrincipal()).andReturn(principal);
    // EasyMock.expect(connectionFactory.getTrackingMap((StackTraceElement[]) EasyMock.anyObject())).andReturn(trackingMap);
    // EasyMock.expect(connectionFactory.getConnection(EasyMock.eq(AccumuloConnectionFactory.Priority.LOW), EasyMock.eq(trackingMap))).andReturn(connector);
    // connectionFactory.returnConnection(connector);
    // PowerMock.replayAll();
    //
    // datawave.webservice.model.Model model = bean.getModel("MODEL2", (String) null);
    // PowerMock.verifyAll();
    //
    // MODEL_ONE.setName("MODEL2");
    // Assert.assertEquals(MODEL_ONE, model);
    //
    // }
    //
    // @Test
    // public void testCheckModelName() throws Exception {
    // String modelTableName = Whitebox.invokeMethod(bean, "checkModelTableName", (String) null);
    // Assert.assertEquals(ModelBean.DEFAULT_MODEL_TABLE_NAME, modelTableName);
    // modelTableName = "foo";
    // String response = Whitebox.invokeMethod(bean, "checkModelTableName", modelTableName);
    // Assert.assertEquals(modelTableName, response);
    //
    // }
    //
    // private void dumpModels() throws Exception {
    // System.out.println("******************* Start Dump Models **********************");
    // Set<Authorizations> cbAuths = new HashSet<>();
    // for (Collection<String> auths : principal.getAuthorizations()) {
    // cbAuths.add(new Authorizations(auths.toArray(new String[auths.size()])));
    // }
    //
    // Scanner scanner = ScannerHelper.createScanner(connector, ModelBean.DEFAULT_MODEL_TABLE_NAME, cbAuths);
    // for (Entry<Key,Value> entry : scanner) {
    // System.out.println(entry.getKey());
    // }
    //
    // System.out.println("******************* End Dump Models **********************");
    // }
    
}
