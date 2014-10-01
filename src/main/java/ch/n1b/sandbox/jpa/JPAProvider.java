package ch.n1b.sandbox.jpa;

import com.google.common.collect.Maps;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.internal.jpa.deployment.SEPersistenceUnitInfo;
import org.eclipse.persistence.jpa.PersistenceProvider;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitTransactionType;

/**
 * Created on 28.09.2014.
 * @author Thomas
 */
public class JPAProvider implements JPAService {

    private static final String PUNIT_NAME = "sponge";

    private Map<String,String> datasourceProperties;
    private String jdbcUrlPrefix;

    public JPAProvider(String jdbcUrlPrefix) {
        this.jdbcUrlPrefix = jdbcUrlPrefix;

        // Load settings from sponge config, so we don't need a persistence.xml
        datasourceProperties = Maps.newHashMap();
        datasourceProperties.put(PersistenceUnitProperties.JDBC_DRIVER, "com.mysql.jdbc.Driver");
        datasourceProperties.put(PersistenceUnitProperties.JDBC_USER, "root");
        datasourceProperties.put(PersistenceUnitProperties.JDBC_PASSWORD, "1234");
        //datasourceProperties.put(DDL_GENERATION, "create-tables");
    }


    @Override
    public EntityManager getEntityManager(String databaseId) {
        Map<String,String> mOptions = Maps.newHashMap();
        mOptions.putAll(datasourceProperties);
        // try to create tables if not exsisting
        mOptions.put(PersistenceUnitProperties.DDL_GENERATION, "create-tables");
        return getEntityManagerFactory(databaseId, mOptions).createEntityManager();
    }


    @Override
    public EntityManagerFactory getEntityManagerFactory(String databaseId, Map<String, String> options) {
        //---- assemble the persistence.xml / properties
        Map<String,String> mOptions = Maps.newHashMap();
        // any custom options?
        if(options!=null) {
            mOptions.putAll(options);
        }
        // overwrite with properties from sponge if necessary
        mOptions.putAll(datasourceProperties);
        // set the URL: PREFIX + databaseId
        mOptions.put(PersistenceUnitProperties.JDBC_URL,generateJDBCUrl(databaseId));

        SEPersistenceUnitInfo punit = new SEPersistenceUnitInfo();
        punit.setPersistenceProviderClassName("org.eclipse.persistence.jpa.PersistenceProvider");
        punit.setTransactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);
        punit.setPersistenceUnitName("sponge-eclipselink");
        punit.setClassLoader(ClassLoader.getSystemClassLoader());

        Path currentRelativePath = Paths.get("");
        try {
            punit.setPersistenceUnitRootUrl(currentRelativePath.toUri().toURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        punit.setManagedClassNames(Collections.singletonList(AnEntity.class.getCanonicalName()));

        Properties properties = new Properties();
        properties.putAll(mOptions);
        punit.setProperties(properties);
        PersistenceProvider provider = new PersistenceProvider();

        return provider.createContainerEntityManagerFactory(punit, mOptions);
    }

    private String generateJDBCUrl(String databaseId){
        return jdbcUrlPrefix.concat(databaseId);
    }
}
