package msdchallenge.main;

import msdchallenge.core.Processor;
import msdchallenge.repository.IRepositoryWrapper;
import msdchallenge.repository.OwlimRepositoryWrapper;
import msdchallenge.repository.Parameters;
import msdchallenge.repository.SesameNativeRepositoryWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Kaggle {

	private static final Logger LOG = LoggerFactory.getLogger(Kaggle.class);

	public static void main(String[] args) {
		// Parse all the parameters
		Parameters params = new Parameters(args);
		params.setDefaultValue(OwlimRepositoryWrapper.PARAM_CONFIG, "config/owlim_test.ttl");
		params.setDefaultValue(OwlimRepositoryWrapper.PARAM_SHOWRESULTS, "true");
		params.setDefaultValue(OwlimRepositoryWrapper.PARAM_SHOWSTATS, "false");
		params.setDefaultValue(OwlimRepositoryWrapper.PARAM_UPDATES, "false");
//		params.setDefaultValue(RepositoryWrapper.PARAM_PRELOAD, PREFIX + "preload");
//		params.setDefaultValue(OwlimRepositoryWrapper.PARAM_QUERYFILE, "scooter");

		LOG.info("Using parameters:");
		LOG.info(params.toString());

//		IRepositoryWrapper wrapper = new OwlimRepositoryWrapper(params.getParameters());
		IRepositoryWrapper wrapper = new SesameNativeRepositoryWrapper();

		Processor p = new Processor(wrapper);
		p.doWork();
		
		wrapper.shutdown();
	}

}
