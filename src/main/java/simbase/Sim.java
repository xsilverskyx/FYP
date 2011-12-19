/**
 *
 */
package simbase;

import generatorbase.AgentModel;
import generatorbase.EntityManager;
import generatorbase.ProductModel;

import java.util.ArrayList;
import java.util.Random;

import modelbase.Entity;

import org.joda.time.DateTime;

import productbase.Product;
import productbase.ProductManager;
import agentbase.Agent;
import agentbase.AgentManager;
import agentbase.Buyer;
import agentbase.Seller;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;

import configbase.ProductConfig;
import configbase.SimConfig;
import core.BaseObject;

/**
 * @author akai Main file of the project.
 */
public class Sim extends BaseObject {
	int					timeStep;
	int					quantityAssigned;
	int					numSellerAssigned;
	AgentManager		agentManager;
	AgentModel			agentModel;
	SQLiteConnection	db;
	SimConfig			simConfig;
	Scheduler			scheduler;
	ProductManager		prodManager;
	ProductModel		prodModel;
	String				sessionId;
	InventoryManager	inventoryManager;
	Bank				bank;
	TransactionManager	transactionManager;
	RatingManager		ratingManager;

	public TransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
		this.transactionManager.setSim(this);
	}

	public int getNumSellerAssigned() {
		return numSellerAssigned;
	}

	public InventoryManager getInventoryManager() {
		return inventoryManager;
	}

	public AgentManager getAgentManager() {
		return agentManager;
	}

	public int getQuantityAssigned() {
		return quantityAssigned;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
		this.agentManager.setSessionId(this.sessionId);
		bank.setAgentManger(agentManager);
	}

	public SimConfig getSimConfig() {
		return simConfig;
	}

	public void setSimConfig(SimConfig simConfig) {
		this.simConfig = simConfig;
		this.prodManager.setConfig(simConfig.getProdConfig());
		this.agentManager.setConfig(simConfig.getAgentConfig());
	}

	public Scheduler getScheduler() {
		return scheduler;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	public ProductManager getProdManager() {
		return prodManager;
	}

	public void setProdManager(ProductManager prodManager) {
		this.prodManager = prodManager;
		this.prodManager.setSessionId(this.sessionId);
	}

	public Sim() {
		this.bank = new Bank();
		_constructor_helper();
	}

	public Sim(Bank bank) {
		this.bank = bank;
		_constructor_helper();
	}

	/* A wrapper of necessity functions when initiating a new object */
	private void _constructor_helper() {
		numSellerAssigned = 0;
		sessionId = (new DateTime()).toString();
		db = setUpDb();
		this.initObjects();
		this.setDb(db);
	}

	/**
	 * @return
	 */
	private SQLiteConnection setUpDb() {
		SQLiteConnection conn = null;
		return conn;
	}

	public void initObjects() {
		ratingManager = new RatingManager();
		prodManager = new ProductManager();
		agentManager = new AgentManager();
		inventoryManager = new InventoryManager();
		agentModel = new AgentModel();
		simConfig = new SimConfig();
		scheduler = new Scheduler();
		transactionManager = new TransactionManager();
		transactionManager.sim = this;
		inventoryManager.sim = this;
	}

	public void initialize() throws Exception {
		agentManager.setSessionId(sessionId);
		prodManager.setSessionId(sessionId);
		agentModel.setConfig(simConfig.getAgentConfig());
		prodModel = new ProductModel(simConfig.getProdConfig());
		prodModel.generate(prodManager);
		agentModel.generate(agentManager);
		for (Entity agent : agentManager.getBuyers().getAll()) {
			((Agent) agent).setInventoryManager(this.inventoryManager);
		}
		bank.setAgentManger(agentManager);
	}

	public static void main(String[] args) {
		int timeStep = 0;
		int maxTimeStep;
		Sim sim = new Sim();
		try {
			sim.initialize();
			sim.assignProducts();
			maxTimeStep = sim.simConfig.getMaxTimestep();
			while (timeStep<maxTimeStep) {
				sim.advanceTime();
				/* Market movement will be here */
			}
		} catch (SQLiteException e) {
			sim.logger.error("Error with the database. Please review the log file");
		} catch (Exception e) {
			sim.logger.error("Can't initialize the simulation");
		} finally {
			sim.logger.error("Exiting...");
			System.exit(1);
		}
	}

	/**
	 * At each time step, all balance will be credit a fixed amount of money
	 */
	private void topUpBalance() {
		// TODO Auto-generated method stub

	}

	/**
	 * Assign products to sellers base on categories, as in the market, usually
	 * a seller will sell related products.
	 * 
	 * @throws Exception
	 */
	public void assignProducts() throws Exception {
		int numProd;
		int prodNum;
		Inventory inventory;
		int quantityAssignThres = ((ProductConfig) prodManager.getConfig())
				.getQuantityAssignmentThreadshold();
		Product prod, tmpProd = null;
		quantityAssigned = 0;
		Seller seller;
		Random prodRandom = new Random();
		Random prodNumRandom = new Random();
		Random prodPriceRandom = new Random();
		EntityManager sellers = agentManager.getSellers();
		logger.info("Start assigning products to sellers");
		numSellerAssigned = 0;
		inventoryManager.beginTransaction();
		for (Entity e : sellers.getAll()) {
			seller = (Seller) e;
			/* Pick products until a non-zero number of items is picked */
			/* No more products to assign, terminate */
			if (prodManager.getSize()==0) {
				logger.info("Products exhausted, no more products to be assigned");
				logger.info("Total of "+(numSellerAssigned)+" sellers were assigned products");
				break;
			}

			/* Pick a random product */
			prodNum = prodRandom.nextInt(prodManager.getSize());
			prod = (Product) prodManager
					.get((String) prodManager.getAvailableProducts().toArray()[prodNum]);
			while (prod==null||prod.getQuantity()==0) {
				prodNum = prodRandom.nextInt(prodManager.getSize());
				prod = (Product) prodManager.get((String) prodManager.getAvailableProducts()
						.toArray()[prodNum]);
			}
			tmpProd = new Product(prod);
			if (prod.getQuantity()<=quantityAssignThres)
				numProd = prod.getQuantity();
			else {
				numProd = prodNumRandom.nextInt(prod.getQuantity());
				while (numProd==0) {
					numProd = prodNumRandom.nextInt(prod.getQuantity());
					if (numProd==0) {
						numProd = prod.getQuantity();
						break;
					}
				}
			}
			quantityAssigned += numProd;
			tmpProd.setQuantity(numProd);
			tmpProd.setPriceMin(prod.getPriceMin());
			tmpProd.setPriceMax(prod.getPriceMin()+prodPriceRandom.nextDouble()
					*(prod.getPriceMax()-prod.getPriceMin()));
			inventory = new Inventory(seller, tmpProd, tmpProd.getQuantity(),
					tmpProd.getPriceMax(), 0);
			inventory.setValue(seller.initValue(tmpProd));
			inventoryManager.add(inventory);
			prodManager.update(prod);
			if (prod.getQuantity()==0) {
				logger.debug("Product "+prod.getName()+" is up!");
			}
			logger.debug(String.format("Assigned product %-3s(x%5d) to seller %s", prod.getName(),
					tmpProd.getQuantity(), seller.getName()));
			numSellerAssigned++;

		}
		inventoryManager.commitTransaction();
		logger.info("Finished assigning products to sellers");
	}

	/**
	 * @param db2
	 */
	public void setDb(SQLiteConnection db) {
		// TODO Auto-generated method stub
		this.db = db;
		agentManager.setDb(db);
		prodManager.setDb(db);
		inventoryManager.setDb(db);
		bank.setDb(db);

	}

	/**
	 * Advance the simulation. At the beginning of each time step, all buyers
	 * will be credited an amount of money
	 * 
	 * @throws SQLiteException
	 * 
	 */
	public void advanceTime() throws SQLiteException {
		scheduler.advanceTime();
		bank.creditAllBuyers(simConfig.getCreditPerTurn());
	}

	/**
	 * @param name
	 * @return
	 */
	public double getBalance(String accountName) {
		return bank.getBalance(accountName);
	}

	public double getBalance(Agent agent) {
		return bank.getBalance(agent.getName());
	}

	public void run() throws Exception {
		Buyer buyer;
		Seller seller;
		Product product;
		Transaction transaction;
		initialize();
		assignProducts();
		int maxTimeStep = simConfig.getMaxTimestep();
		Execution execution;
		logger.info("*** Simulation is running...");
		while (timeStep<maxTimeStep) {
			advanceTime();
			for (Entity e : getAgentManager().getBuyers().getAll()) {
				buyer = (Buyer) e;
				transaction = buyer.makeTransaction();
				if (transaction!=null) {
					execution = transactionManager.addTransaction(transaction);
					if (execution!=null) {
						logger.debug(execution);
					}
				}
			}
			transactionManager.processTransactions();
			// prodManager.reportQuantity();
			timeStep++;
			scheduler.finalizeTimeStep();
		}
		logger.info("*** Rating Report ***");
		ratingManager.reportRating();
		logger.info("*** Balance Report ***");
		bank.reportBalance(this.agentManager.getBuyers().getEntitiesNames());
		logger.info("*** Simulation has finished!");
	}
}
