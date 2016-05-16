/*
    Copyright (c) 2012  Mustafa MISIR, KU Leuven - KAHO Sint-Lieven, Belgium
 	
 	This file is part of GIHH v1.0.
 	
    GIHH is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    	
*/

package be.kuleuven.kahosl.hyperheuristic;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;
import BinPacking.BinPacking;
import FlowShop.FlowShop;
import PDP.HPModel;
import PDP.PDP;
import SAT.SAT;
import VRP.VRP;
import be.kuleuven.kahosl.acceptance.AcceptanceCriterionType;
import be.kuleuven.kahosl.problem.ProblemName;
import be.kuleuven.kahosl.selection.SelectionMethodType;
import be.kuleuven.kahosl.util.Print;
import be.kuleuven.kahosl.util.Vars;
import be.kuleuven.kahosl.util.WriteInfo;
import travelingSalesmanProblem.TSP;

/**
 * This class shows how to run GIHH
 */
public class GIHHCompleteRun {

	/** A logger to write various running information/comments/errors **/
	private static final Logger log = LoggerFactory.getLogger(GIHHCompleteRun.class);

	/** Seed for random number generator **/
	private static long seed = 1234;

	/** Heuristic selection mechanisms **/
	private static SelectionMethodType[] selectionList = { SelectionMethodType.AdaptiveLimitedLAassistedDHSMentorSTD };

	/** Move acceptance mechanisms **/
	private static AcceptanceCriterionType[] acceptanceList = {
			AcceptanceCriterionType.AdaptiveIterationLimitedListBasedTA };

	/** Problems to solve **/
	private static ProblemName[] problemList = { ProblemName.PDP,
			// ProblemName.BinPacking,
			// ProblemName.FlowShop,
			// ProblemName.PersonelScheduling,
			// ProblemName.TravellingSalesman,
			// ProblemName.VehicleRouting
	};

	/** Number of instances for each problem **/
	// private static int[] instanceNumber = {12,12,12,12,10,10};
	private static int[] instanceNumber = { 7 };

	/**
	 * Main function
	 * 
	 * @param args
	 *            arguments for the main function
	 */
	public static void main(String[] args) {// updateSelectionElements

		// Vidal: Adding this variable in order to execute GIHH on the cluster
		// at UFPR CBIOs lab this variable will be used to generated the seed
		// number
		int trialNumber = -1;
		if (args.length > 0) {
			String[] instances = args[0].split(",");
			instanceNumber = new int[instances.length];
			for (int i = 0; i < instances.length; i++) {
				instanceNumber[i] = Integer.valueOf(instances[i]);
			}
			trialNumber = Integer.valueOf(args[1]);

		}

		Vars.totalExecutionTime = 600000;// 553000 /* Total execution tim
											// (stopping condition) */
		Vars.numberOfTrials = 1; /*
									 * Number of runs for each hyper-heuristic
									 * on each problem instance
									 */

		Print.hyperheuristic = false; /*
										 * Whether to print or not general
										 * hyper-heuristic logs
										 */
		Print.iterationBasedInfo = false; /*
											 * Iteration number to capture the
											 * Information about the state of
											 * the search to log/to write to a
											 * file
											 */

		String resultFileName; /* Common part of the result files' names */

		/*
		 * Date information to create a folder with q unique name to save the
		 * generated files
		 */
		Date today = new Date();
		Format dateFormatter = new SimpleDateFormat("ddMMyyyyHHmmss");

		/* Folder name based on the generated date information */
		WriteInfo.resultSubFolderName = dateFormatter.format(today);

		/* For each problem */
		for (int pr = 0; pr < problemList.length; pr++) {
			/* For each problem instance */
			for (int ins = 0; ins < instanceNumber.length; ins++) {
				/* For each heuristic selection mechanism */
				for (int hs = 0; hs < selectionList.length; hs++) {
					/* For each move acceptance mechanism */
					for (int ac = 0; ac < acceptanceList.length; ac++) {
						/* For each trial */
						for (int tr = 0; tr < Vars.numberOfTrials; tr++) {
							/*
							 * Determine the common part of the result files'
							 * names
							 */
							// Vidal: Changing the result filename with the
							// trial number
							resultFileName = selectionList[hs].toString() + "_" + acceptanceList[ac].toString() + "_"
									+ problemList[pr].toString().replace(" ", "") + "_INST" + instanceNumber[ins]
									+ "_TM" + (int) (Vars.totalExecutionTime / 1000.0) + "_TTR" + Vars.numberOfTrials
									+ "_TR" + (trialNumber + 1) + "_";

							log.info(" @@ ResultFileName: " + resultFileName);

							ProblemDomain problem = null;

							/* Seed for each trial */
							// Vidal: Also changed tr to trialNumber in order to
							// generate different seeds on the cluster
							long tempSeed = seed * (trialNumber + 1);

							/* Create a problem object */
							if (problemList[pr] == ProblemName.MaxSAT) {
								problem = new SAT(tempSeed);
							} else if (problemList[pr] == ProblemName.BinPacking) {
								problem = new BinPacking(tempSeed);
							} else if (problemList[pr] == ProblemName.FlowShop) {
								problem = new FlowShop(tempSeed);
							} else if (problemList[pr] == ProblemName.PDP) {
								problem = new PDP(tempSeed, HPModel.TWO_DIMENSIONAL, 1, 3, 2);
							}
							// else if(problemList[pr] ==
							// ProblemName.PersonelScheduling){
							// problem = new PersonnelScheduling(tempSeed);
							// }
							else if (problemList[pr] == ProblemName.TravellingSalesman) {
								problem = new TSP(tempSeed);
							} else if (problemList[pr] == ProblemName.VehicleRouting) {
								problem = new VRP(tempSeed);
							} else {
								log.error("Unrecognised problem ! " + problem.toString());
								System.exit(-1);
							}

							/* Load the problem instance to solve */
							problem.loadInstance(instanceNumber[ins]);

							/* Set re-initialisation related parameters */
							Vars.restartSearch = false;
							Vars.useOverallBestSln = false;
							Vars.noMoreRestart = false;

							System.out.println(" ## GIHH STARTED >> HyperHeuristic " + problemList[pr] + " instance #"
									+ instanceNumber[ins] + " (RUN = " + (trialNumber + 1) + ") (SEED = " + tempSeed
									+ ")");

							/* Construct an hyper-heuristic object */
							HyperHeuristic hh = new GIHH(tempSeed, problem.getNumberOfHeuristics(),
									Vars.totalExecutionTime, resultFileName, selectionList[hs], acceptanceList[ac]);

							/* Set the total execution time limit */
							hh.setTimeLimit(Vars.totalExecutionTime);
							/*
							 * Load the problem (+instance) for hyper-heuristic
							 */
							hh.loadProblemDomain(problem);
							/* Start solving the target problem instance */
							hh.run();

							System.out.println(" ## GIHH FINISHED");
						}
					}
				}
			}
		}
	}
}
