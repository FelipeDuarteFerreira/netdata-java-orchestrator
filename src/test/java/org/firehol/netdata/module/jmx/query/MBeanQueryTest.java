package org.firehol.netdata.module.jmx.query;

import org.firehol.netdata.model.Dimension;
import org.firehol.netdata.module.jmx.exception.JmxMBeanServerQueryException;
import org.firehol.netdata.module.jmx.utils.MBeanServerUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.management.*;
import javax.management.openmbean.CompositeData;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MBeanServerUtils.class)
public class MBeanQueryTest {

    private final MBeanServerConnection mBeanServer = mock(MBeanServerConnection.class);

    @Test
    public void testNewInsctanceWithCheckInteger() throws AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException, IOException, JmxMBeanServerQueryException {
        when(mBeanServer.getAttribute(ObjectName.WILDCARD, "Attribute")).thenReturn(1234);

        final MBeanQuery mBeanQuery = MBeanQuery.newInstance(mBeanServer, ObjectName.WILDCARD, "Attribute");

        assertInstanceOf(MBeanIntegerQuery.class, mBeanQuery);

        verify(mBeanServer).getAttribute(ObjectName.WILDCARD, "Attribute");
    }

    @Test
    public void testNewInsctanceWithCheckLong() throws JmxMBeanServerQueryException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException, IOException {
        when(mBeanServer.getAttribute(ObjectName.WILDCARD, "Attribute")).thenReturn(1234L);

        final MBeanQuery mBeanQuery = MBeanQuery.newInstance(mBeanServer, ObjectName.WILDCARD, "Attribute");

        assertInstanceOf(MBeanLongQuery.class, mBeanQuery);
    }

    @Test
    public void testNewInstanceInteger() {
        final MBeanQuery mBeanQuery = MBeanQuery.newInstance(ObjectName.WILDCARD, "attributeName", Integer.class);

        assertInstanceOf(MBeanIntegerQuery.class, mBeanQuery);
    }

    @Test
    public void testNewInstanceLong() {
        final MBeanQuery mBeanQuery = MBeanQuery.newInstance(ObjectName.WILDCARD, "attributeName", Long.class);

        assertInstanceOf(MBeanLongQuery.class, mBeanQuery);
    }

    @Test
    public void testNewInstanceDouble() {
        final MBeanQuery mBeanQuery = MBeanQuery.newInstance(ObjectName.WILDCARD, "attributeName", Double.class);

        assertInstanceOf(MBeanDoubleQuery.class, mBeanQuery);
    }

    @Test
    public void testNewInstanceCompositeData() {
        final MBeanQuery mBeanQuery = MBeanQuery.newInstance(ObjectName.WILDCARD, "attribute.compkey", CompositeData.class);

        assertInstanceOf(MBeanCompositeDataQuery.class, mBeanQuery);
    }

    private void assertInstanceOf(final Class<?> expectedClass, final MBeanQuery mBeanQuery) {
        if (!expectedClass.isInstance(mBeanQuery)) {
            fail(String.format("%s should be instance of %s but is instance of %s", mBeanQuery.toString(),expectedClass.toString(), mBeanQuery.getClass().toString()));
        }
    }

    @Test
    public void testQueryLong() throws JmxMBeanServerQueryException, MalformedObjectNameException {
        testQuery(1234L);
    }

    @Test
    public void testQueryDouble() throws JmxMBeanServerQueryException, MalformedObjectNameException {
        testQuery(12.34);

    }

    @Test
    public void TestQueryInteger() throws JmxMBeanServerQueryException, MalformedObjectNameException {
        testQuery(1234);
    }

    public void testQuery(Object queryResult) throws JmxMBeanServerQueryException, MalformedObjectNameException {
        // prepare
        final ObjectName name = new ObjectName("*:type=MBean");
        final MBeanQuery query = MBeanQuery.newInstance(name, "MBeanAttributeName", queryResult.getClass());
        final Dimension dim1 = new Dimension();
        dim1.setName("Dimension 1");
        query.getDimensions().add(dim1);
        final Dimension dim2 = new Dimension();
        dim2.setName("Dimension 2");
        query.getDimensions().add(dim2);

        PowerMockito.mockStatic(MBeanServerUtils.class);
        when(MBeanServerUtils.getAttribute(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(queryResult);

        // test
        query.query(mBeanServer);

        // assert
        for(Dimension dimension : query.getDimensions()) {
            Assert.assertEquals(dimension.getName(), (Long) 1234L, dimension.getCurrentValue());
        }
    }

    @Test
    public void testAddDimension() {
        final MBeanQuery query = MBeanQuery.newInstance(ObjectName.WILDCARD, "attribute", Long.class);
        final Dimension dimension = new Dimension();

        query.addDimension(dimension, "attribute");

        assertEquals(dimension, query.getDimensions().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDimensionAttributeNotMatch() {
        final MBeanQuery query = MBeanQuery.newInstance(ObjectName.WILDCARD, "attribute", Long.class);
        final Dimension dimension = new Dimension();

        query.addDimension(dimension, "no match");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDimensionAttributeNull() {
        final MBeanQuery query = MBeanQuery.newInstance(ObjectName.WILDCARD, "attribute", Long.class);
        final Dimension dimension = new Dimension();

        query.addDimension(dimension, null);
    }

    @Test()
    public void testAddDimensionCompositeData() {
        final MBeanQuery query = MBeanQuery.newInstance(ObjectName.WILDCARD, "attribute", CompositeData.class);
        final Dimension dimension = new Dimension();

        query.addDimension(dimension, "attribute.key");

        assertEquals(dimension, query.getDimensions().get(0));
    }

}