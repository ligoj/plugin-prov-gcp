package org.ligoj.app.plugin.prov.google;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.app.AbstractAppTest;
import org.ligoj.app.model.Node;
import org.ligoj.app.model.Project;
import org.ligoj.app.model.Subscription;
import org.ligoj.app.plugin.prov.ProvResource;
import org.ligoj.app.plugin.prov.QuoteVo;
import org.ligoj.app.plugin.prov.model.ProvInstancePrice;
import org.ligoj.app.plugin.prov.model.ProvInstancePriceTerm;
import org.ligoj.app.plugin.prov.model.ProvInstanceType;
import org.ligoj.app.plugin.prov.model.ProvLocation;
import org.ligoj.app.plugin.prov.model.ProvQuote;
import org.ligoj.app.plugin.prov.model.ProvQuoteInstance;
import org.ligoj.app.plugin.prov.model.ProvQuoteStorage;
import org.ligoj.app.plugin.prov.model.ProvStoragePrice;
import org.ligoj.app.plugin.prov.model.ProvStorageType;
import org.ligoj.app.plugin.prov.model.Rate;
import org.ligoj.app.plugin.prov.model.VmOs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test class of {@link ProvGoogleResource}
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/application-context-test.xml")
@Rollback
@Transactional
public class ProvGoogleResourceTest extends AbstractAppTest {

	@Autowired
	private ProvGoogleResource resource;

	@Autowired
	private ProvResource provResource;

	private int subscription;

	@BeforeEach
	public void prepareData() throws IOException {
		persistSystemEntities();
		persistEntities("csv",
				new Class[] { Node.class, Project.class, Subscription.class, ProvLocation.class, ProvQuote.class, ProvStorageType.class,
						ProvInstancePriceTerm.class, ProvInstanceType.class, ProvInstancePrice.class, ProvQuoteInstance.class,
						ProvQuoteStorage.class },
				StandardCharsets.UTF_8.name());
		subscription = getSubscription("gStack", ProvGoogleResource.SERVICE_KEY);
	}

	@Test
	public void getConfiguration() {
		final QuoteVo vo = provResource.getConfiguration(subscription);
		Assertions.assertEquals("quote1", vo.getName());
		Assertions.assertNotNull(vo.getId());
		Assertions.assertNotNull(vo.getCreatedBy());
		Assertions.assertNotNull(vo.getCreatedDate());
		Assertions.assertNotNull(vo.getLastModifiedBy());
		Assertions.assertNotNull(vo.getLastModifiedDate());

		// Check compute
		final List<ProvQuoteInstance> instances = vo.getInstances();
		Assertions.assertEquals(3, instances.size());
		final ProvQuoteInstance quoteInstance = instances.get(0);
		Assertions.assertNotNull(quoteInstance.getId());
		Assertions.assertEquals("f1-micro-LINUX-Default-0.0086", quoteInstance.getName());
		final ProvInstancePrice instancePrice = quoteInstance.getPrice();
		Assertions.assertEquals(0.0086, instancePrice.getCost(), 0.0001);
		Assertions.assertEquals(VmOs.LINUX, instancePrice.getOs());
		Assertions.assertNotNull(instancePrice.getTerm().getId());
		Assertions.assertEquals(1, instancePrice.getTerm().getPeriod());
		Assertions.assertEquals("Default", instancePrice.getTerm().getName());
		final ProvInstanceType instance = instancePrice.getType();
		Assertions.assertNotNull(instance.getId().intValue());
		Assertions.assertEquals("f1-micro", instance.getName());
		Assertions.assertEquals(0.2, instance.getCpu(), 0.01);
		Assertions.assertEquals(614, instance.getRam().intValue());
		Assertions.assertFalse(instance.getConstant());

		Assertions.assertEquals("SQL Server Enterprise", instances.get(2).getPrice().getLicense());

		// Check storage
		final List<ProvQuoteStorage> storages = vo.getStorages();
		Assertions.assertEquals(4, storages.size());
		final ProvQuoteStorage quoteStorage = storages.get(0);
		Assertions.assertNotNull(quoteStorage.getId());
		Assertions.assertEquals("server1-root", quoteStorage.getName());
		Assertions.assertEquals(20, quoteStorage.getSize().intValue());
		Assertions.assertNotNull(quoteStorage.getQuoteInstance());
		final ProvStoragePrice storage = quoteStorage.getPrice();
		Assertions.assertNotNull(storage.getId());
		Assertions.assertEquals(0.04, storage.getCostGb(), 0.001);
		Assertions.assertEquals(0, storage.getCost(), 0.001);
		Assertions.assertEquals("Standard provisioned space", storage.getType().getName());
		Assertions.assertEquals(Rate.GOOD, storage.getType().getLatency());

		// Not attached storage
		Assertions.assertNull(storages.get(3).getQuoteInstance());

	}

	@Test
	public void getKey() {
		Assertions.assertEquals("service:prov:google", resource.getKey());
	}

}
