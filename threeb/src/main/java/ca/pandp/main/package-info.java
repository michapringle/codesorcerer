/**
 * Created by Micha "Micha Did It" Pringle on Jun 07, 2015.
 * <p>
 *
 * This package contains the main program that invokes the producer plug-ins, and feeds the output to the consumer
 * plug-ins. In this way, developers who want to support additional functionality are required to add either a
 * producer plug-in (to generate the model object), or a consumer plug-in (to consume the model object).
 *
 * Project dependencies should work as follows:
 * <ul>
 * <li>shared package depends on no packages local to this project</li>
 * <li>plugins.producers package depends on shared</li>
 * <li>plugins.consumers package depends on shared</li>
 * <li>main package depends on shared, and plugins.consumers.Consumable, and plugins.producers.Producible</li>
 * </ul>
 *
 */

package ca.pandp.main;