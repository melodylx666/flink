/***********************************************************************************************************************
 *
 * Copyright (C) 2010 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 **********************************************************************************************************************/
package eu.stratosphere.sopremo.sdaa11.frequent_itemsets.dap;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import eu.stratosphere.sopremo.CompositeOperator;
import eu.stratosphere.sopremo.ElementarySopremoModule;
import eu.stratosphere.sopremo.InputCardinality;
import eu.stratosphere.sopremo.OutputCardinality;
import eu.stratosphere.sopremo.SopremoModule;
import eu.stratosphere.sopremo.Source;
import eu.stratosphere.sopremo.sdaa11.frequent_itemsets.dap.json.FrequentItemsetNodes;
import eu.stratosphere.sopremo.sdaa11.frequent_itemsets.util.Baskets;
import eu.stratosphere.sopremo.testing.SopremoTestPlan;
import eu.stratosphere.sopremo.type.ArrayNode;
import eu.stratosphere.sopremo.type.IArrayNode;
import eu.stratosphere.sopremo.type.IJsonNode;
import eu.stratosphere.sopremo.type.ObjectNode;
import eu.stratosphere.sopremo.type.TextNode;

/**
 * @author skruse
 * 
 */
public class ShrinkBasketsTest {

	private static final String SMALL_BASKETS_PATH = "src/test/resources/frequent_itemsets/small_baskets";

	@Test
	public void testSelection() throws IOException {

		final SopremoTestPlan plan = new SopremoTestPlan(new TestOperator());
		final List<IJsonNode> baskets = Baskets.loadBaskets(SMALL_BASKETS_PATH);

		for (final IJsonNode basket : baskets)
			plan.getInput(0).add(basket);

		ObjectNode expectedNode = new ObjectNode();
		IArrayNode itemsNode = new ArrayNode();
		itemsNode.add(new TextNode("apple"));
		itemsNode.add(new TextNode("banana"));
		itemsNode.add(new TextNode("cherry"));
		FrequentItemsetNodes.write(expectedNode, itemsNode);
		plan.getExpectedOutput(0).add(expectedNode);

		expectedNode = new ObjectNode();
		itemsNode = new ArrayNode();
		itemsNode.add(new TextNode("apple"));
		itemsNode.add(new TextNode("banana"));
		FrequentItemsetNodes.write(expectedNode, itemsNode);
		plan.getExpectedOutput(0).add(expectedNode);
		
		expectedNode = new ObjectNode();
		itemsNode = new ArrayNode();
		itemsNode.add(new TextNode("apple"));
		itemsNode.add(new TextNode("banana"));
		itemsNode.add(new TextNode("cherry"));
		FrequentItemsetNodes.write(expectedNode, itemsNode);
		plan.getExpectedOutput(0).add(expectedNode);
		
		expectedNode = new ObjectNode();
		itemsNode = new ArrayNode();
		itemsNode.add(new TextNode("banana"));
		itemsNode.add(new TextNode("cherry"));
		FrequentItemsetNodes.write(expectedNode, itemsNode);
		plan.getExpectedOutput(0).add(expectedNode);

		plan.run();

		int count = 0;
		for (@SuppressWarnings("unused")
		final IJsonNode node : plan.getActualOutput(0))
			count++;
		Assert.assertEquals(4, count);
	}

	@InputCardinality(value = 1)
	@OutputCardinality(value = 1)
	public static class TestOperator extends CompositeOperator<TestOperator> {

		private static final long serialVersionUID = -2919504704183719236L;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * eu.stratosphere.sopremo.CompositeOperator#asElementaryOperators()
		 */
		@Override
		public ElementarySopremoModule asElementaryOperators() {
			final SopremoModule module = new SopremoModule(this.getName(), 1, 1);
			final Source input = module.getInput(0);

			final SelectFrequentItems selectFrequentItems = new SelectFrequentItems()
					.withInputs(input);
			selectFrequentItems.setMinSupport(3);

			final GroupFrequentItemsets groupFrequentItemsets = new GroupFrequentItemsets()
					.withInputs(selectFrequentItems);

			final ShrinkBaskets shrinkBaskets = new ShrinkBaskets().withInputs(
					input, groupFrequentItemsets);

			module.getOutput(0).setInput(0, shrinkBaskets);
			return module.asElementary();
		}

	}

}
