package consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import economy_simulator.Market;
import goods.DefaultGoodsCollection;
import goods.Good;
import goods.GoodFactory;
import population.Person;

public class ConsumerTest {

	private Consumer consumer;
	private Person person; 
	private Market market;
	Good good;
	final int CONSUMPTION = 4; 

	@BeforeEach
	void setUp() {
		GoodFactory goodFactory = new GoodFactory(new DefaultGoodsCollection());
		ConsumerProfile consumerProfile = new MockConsumptionProfile(goodFactory);
		Market.initialize(goodFactory);
		market = Market.getInstance();
		good = goodFactory.getGood("A");
		// The test assumes that the market initializes with at least 5 of the product
		// we'll use to test
		int quantAtFirst = market.getQuantityGood(good);
		assertTrue(quantAtFirst >= 5);

		person = new Person(0, 0); 
		consumer = new Consumer(consumerProfile, 0, market, person);
		person.addRole(consumer);
	}
	
	@Test
	void testConsumeGood() {
		testConsumeGoodWithSurplus(0);
		testConsumeGoodWithSurplus(20);
	}
	
	@Test
	void testConsumeGoodTooLittleMoney() {
		int quantAtFirst = market.getQuantityGood(good);
		double price = market.requestPriceGood(good);
		person.moneyChanged(price - 0.01);
		double moneyAtFirst = person.moneyInWallet();
		consumer.performRole();
		int quantAfterConsumption = market.getQuantityGood(good);
		double moneyAfterConsumption = person.moneyInWallet(); 
		assertEquals(quantAtFirst, quantAfterConsumption); 
		assertEquals(moneyAtFirst, moneyAfterConsumption, 0.0001);
		
	}


	void testConsumeGoodWithSurplus(double surplus) {
		int quantAtFirst = market.getQuantityGood(good);
		double price = market.requestPriceGood(good);
		person.moneyChanged(price + surplus);
		consumer.performRole();
		int quantAfterConsumption = market.getQuantityGood(good);
		assertEquals(quantAtFirst - quantAfterConsumption, CONSUMPTION);
		assertEquals(person.moneyInWallet(), surplus, 0.0001);
	}
	
	@Test 
	void testConsumeGoodTooMuch() {
		int quantAtFirst = market.getQuantityGood(good);
		double price = market.requestPriceGood(good);
		while(quantAtFirst>=CONSUMPTION) {
			person.moneyChanged(price);
			consumer.performRole();
			quantAtFirst = market.getQuantityGood(good);
		}
		int quantAtSecond = market.getQuantityGood(good);
		consumer.performRole();
		int quantAtLast = market.getQuantityGood(good);
		assertEquals(quantAtSecond, quantAtLast); 
	}

	/**
	 * This consumptionprofile assures a consumption of 5 for good A.
	 * 
	 * @author Marrit Schellekens
	 *
	 */
	private class MockConsumptionProfile extends ConsumerProfile {

		public MockConsumptionProfile(GoodFactory goodFactory) {
			super(goodFactory);
			resetConsumptionProfileToTestParams();
			Good good = goodFactory.getGood("A");
			this.setConsumptionForGood(good, CONSUMPTION);
		}

		public void resetConsumptionProfileToTestParams() {
			for (ConsumerGoodProfile consumerGoodProfile : consumptionProfile) {
				consumerGoodProfile.setConsumption(0);
			}
		}

	}
	
		
}
