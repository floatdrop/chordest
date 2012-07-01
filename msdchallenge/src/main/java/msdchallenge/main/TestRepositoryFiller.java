package msdchallenge.main;

import msdchallenge.repository.IRepositoryWrapper;
import msdchallenge.repository.OwlimRepositoryWrapper;
import msdchallenge.repository.Parameters;
import msdchallenge.repository.SesameNativeRepositoryWrapper;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestRepositoryFiller extends RepositoryFiller {

	private static final Logger LOG = LoggerFactory.getLogger(RepositoryFiller.class);

	public TestRepositoryFiller(Repository repository, RepositoryConnection connection, String prefix) {
		super(repository, connection, prefix);
	}

	public static void main(String[] args) {
		// Parse all the parameters
		Parameters params = new Parameters(args);
		params.setDefaultValue(OwlimRepositoryWrapper.PARAM_CONFIG, "config/owlim_test.ttl");
		params.setDefaultValue(OwlimRepositoryWrapper.PARAM_SHOWRESULTS, "true");
		params.setDefaultValue(OwlimRepositoryWrapper.PARAM_SHOWSTATS, "false");
		params.setDefaultValue(OwlimRepositoryWrapper.PARAM_UPDATES, "false");

		LOG.info("Using parameters:");
		LOG.info(params.toString());

//		IRepositoryWrapper wrapper = new OwlimRepositoryWrapper(params.getParameters());
		IRepositoryWrapper wrapper = new SesameNativeRepositoryWrapper();
		
		RepositoryFiller rf = new RepositoryFiller(wrapper.getRepository(), wrapper.getRepositoryConnection(), "");
		rf.fillRepository();

		wrapper.shutdown();
	}

}
