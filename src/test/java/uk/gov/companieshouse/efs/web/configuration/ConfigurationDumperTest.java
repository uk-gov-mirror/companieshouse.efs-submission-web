package uk.gov.companieshouse.efs.web.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsMapWithSize.anEmptyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class ConfigurationDumperTest {
    private static final String[] PROFILES = {"test"};
    
    private ConfigurationDumper testDumper;
    
    @Mock
    private Logger logger;
    @Mock
    private ApplicationContextEvent event;
    @Mock
    private ApplicationContext context;
    @Mock
    private AbstractEnvironment environment;
    @Mock
    private MutablePropertySources sources;
    @Captor
    private ArgumentCaptor<Map<String, Object>> captor;
    
    private MapPropertySource source;

    @BeforeEach
    void setUp() {
        testDumper = new ConfigurationDumper(logger);

        sources = new MutablePropertySources();
        when(event.getApplicationContext()).thenReturn(context);
        when(context.getEnvironment()).thenReturn(environment);
        when(environment.getActiveProfiles()).thenReturn(PROFILES);
        when(environment.getPropertySources()).thenReturn(sources);
    }

    @Test
    void handleContextRefreshWhenNoPropertySource() {
        testDumper.handleContextRefresh(event);
        
        verify(logger).trace("Active profiles: [test]");
        verify(logger).trace(eq("PROPERTIES"), captor.capture());
        
        assertThat(captor.getValue(), is(anEmptyMap()));
    }
    
    @Test
    void handleContextRefreshWhenNoMapPropertySource() {
        source = new PropertiesPropertySource("properties", new Properties());
        sources.addFirst(source);
        
        testDumper.handleContextRefresh(event);
        
        verify(logger).trace("Active profiles: [test]");
        verify(logger).trace(eq("PROPERTIES"), captor.capture());
        
        assertThat(captor.getValue(), is(anEmptyMap()));
    }
    
    @Test
    void handleContextRefreshWhenMapPropertySource() {
        Map<String, Object> propertyMap = Collections.singletonMap("key", "value");
        
        source = new MapPropertySource("properties", propertyMap);
        sources.addFirst(source);
        
        testDumper.handleContextRefresh(event);
        
        verify(logger).trace("Active profiles: [test]");
        verify(logger).trace(eq("PROPERTIES"), captor.capture());
        
        assertThat(captor.getValue().entrySet(), contains("key", "value"));
    }
}