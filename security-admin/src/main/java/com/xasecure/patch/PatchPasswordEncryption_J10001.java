/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xasecure.patch;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.xasecure.common.StringUtil;
import com.xasecure.db.XADaoManager;
import com.xasecure.entity.XXAsset;
import com.xasecure.service.XAssetService;
import com.xasecure.util.CLIUtil;

@Component
public class PatchPasswordEncryption_J10001 extends BaseLoader {
	static Logger logger = Logger.getLogger(PatchPasswordEncryption_J10001.class);
	int lineCount = 0;
	
	@Autowired
	XADaoManager xaDaoManager;
	
	@Autowired
	StringUtil stringUtil;
	
	@Autowired
	XAssetService xAssetService;
	
	public PatchPasswordEncryption_J10001() {
	}
	

	@Override
	public void printStats() {
		logger.info("Time taken so far:" + timeTakenSoFar(lineCount)
				+ ", moreToProcess=" + isMoreToProcess());
		print(lineCount, "Processed lines");
	}

	@Override
	public void execLoad() {
		encryptLookupUserPassword();
	}

	private void encryptLookupUserPassword() {
		List<XXAsset> xAssetList = xaDaoManager.getXXAsset().getAll();
		String oldConfig=null;
		String newConfig=null;
		for (XXAsset xAsset : xAssetList) {		
			oldConfig=null;
			newConfig=null;
			oldConfig=xAsset.getConfig();
			if(!stringUtil.isEmpty(oldConfig)){
				newConfig=xAssetService.getConfigWithEncryptedPassword(oldConfig,false);
				xAsset.setConfig(newConfig);
				xaDaoManager.getXXAsset().update(xAsset);
			}
			lineCount++;
			logger.info("Lookup Password updated for Asset : "
					+ xAsset.getName());
			logger.info("oldconfig : "+ oldConfig);
			logger.info("newConfig : "+ newConfig);
			print(lineCount, "Total updated assets count : ");
		}
	}

	public static void main(String[] args) {
		logger.info("main()");
		try {
			PatchPasswordEncryption_J10001 loader = (PatchPasswordEncryption_J10001) CLIUtil
					.getBean(PatchPasswordEncryption_J10001.class);
			//loader.init();
			while (loader.isMoreToProcess()) {
				loader.load();
			}
			logger.info("Load complete. Exiting!!!");
			System.exit(0);
		}catch (Exception e) {
			logger.error("Error loading", e);
			System.exit(1);
		}
	}

}
