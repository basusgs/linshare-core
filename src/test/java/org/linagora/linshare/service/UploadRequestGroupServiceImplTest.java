/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2018 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009–2018. Contribute to
 * Linshare R&D by subscribing to an Enterprise offer!” infobox and in the
 * e-mails sent with the Program, (ii) retain all hypertext links between
 * LinShare and linshare.org, between linagora.com and Linagora, and (iii)
 * refrain from infringing Linagora intellectual property rights over its
 * trademarks and commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for LinShare along with this program. If not,
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to LinShare software.
 */

package org.linagora.linshare.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.linagora.linshare.core.domain.constants.LinShareTestConstants;
import org.linagora.linshare.core.domain.constants.UploadRequestStatus;
import org.linagora.linshare.core.domain.entities.AbstractDomain;
import org.linagora.linshare.core.domain.entities.Contact;
import org.linagora.linshare.core.domain.entities.UploadRequest;
import org.linagora.linshare.core.domain.entities.UploadRequestGroup;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.repository.AbstractDomainRepository;
import org.linagora.linshare.core.repository.ContactRepository;
import org.linagora.linshare.core.repository.UserRepository;
import org.linagora.linshare.core.service.UploadRequestGroupService;
import org.linagora.linshare.core.service.UploadRequestService;
import org.linagora.linshare.utils.LinShareWiser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import com.google.common.collect.Lists;

@ContextConfiguration(locations = { "classpath:springContext-datasource.xml",
		"classpath:springContext-repository.xml",
		"classpath:springContext-dao.xml",
		"classpath:springContext-ldap.xml",
		"classpath:springContext-business-service.xml",
		"classpath:springContext-service-miscellaneous.xml",
		"classpath:springContext-service.xml",
		"classpath:springContext-rac.xml",
		"classpath:springContext-fongo.xml",
		"classpath:springContext-storage-jcloud.xml",
		"classpath:springContext-test.xml", })
public class UploadRequestGroupServiceImplTest extends AbstractTransactionalJUnit4SpringContextTests {
	private static Logger logger = LoggerFactory.getLogger(UploadRequestGroupServiceImplTest.class);

	@Qualifier("userRepository")
	@Autowired
	private UserRepository<User> userRepository;

	@Autowired
	private ContactRepository repository;

	@Autowired
	private UploadRequestGroupService uploadRequestGroupService;
	
	@Autowired
	private UploadRequestService uploadRequestService;

	@Autowired
	private AbstractDomainRepository abstractDomainRepository;

	private UploadRequest ure = new UploadRequest();

	private LoadingServiceTestDatas datas;

	private User john;

	private Contact yoda;

	private LinShareWiser wiser;

	public UploadRequestGroupServiceImplTest() {
		super();
		wiser = new LinShareWiser(2525);
	}

	@Before
	public void init() throws Exception {
		logger.debug(LinShareTestConstants.BEGIN_SETUP);
		this.executeSqlScript("import-tests-upload-request.sql", false);
		wiser.start();
		datas = new LoadingServiceTestDatas(userRepository);
		datas.loadUsers();
		john = datas.getUser1();
		AbstractDomain subDomain = abstractDomainRepository.findById(LoadingServiceTestDatas.sqlSubDomain);
		yoda = repository.findByMail("yoda@linshare.org");
		john.setDomain(subDomain);
		// UPLOAD REQUEST CREATE
		ure = initUploadRequest();
		UploadRequestGroup uploadRequestGroup = uploadRequestGroupService.create(john, john, ure, Lists.newArrayList(yoda), "This is a subject",
				"This is a body", false);
		ure = uploadRequestGroup.getUploadRequests().iterator().next();
		logger.debug(LinShareTestConstants.END_SETUP);
	}

	@After
	public void tearDown() throws Exception {
		logger.debug(LinShareTestConstants.BEGIN_TEARDOWN);
		wiser.stop();
		logger.debug(LinShareTestConstants.END_TEARDOWN);
	}

	@Test
	public void createUploadRequest() throws BusinessException {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		UploadRequestGroup uploadRequestGroup = uploadRequestGroupService.create(john, john, ure, Lists.newArrayList(yoda), "This is a subject",
				"This is a body", false);
		Assert.assertNotNull(uploadRequestGroup);
		wiser.checkGeneratedMessages();
		logger.debug(LinShareTestConstants.END_TEST);
	}

	@Test
	public void findAll() throws BusinessException {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		List<UploadRequestGroup> groups = uploadRequestGroupService.findAll(john, john, null);
		Assert.assertNotNull(groups.get(0));
		logger.debug(LinShareTestConstants.END_TEST);
	}

	@Test
	public void findFiltred() throws BusinessException {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		Date tomorrow = calendar.getTime();
		ure.setActivationDate(tomorrow);
		uploadRequestGroupService.create(john, john, ure, Lists.newArrayList(yoda), "This is a subject", "This is a body", false);
		List<UploadRequestGroup> groups = uploadRequestGroupService.findAll(john, john, Lists.newArrayList(UploadRequestStatus.ENABLED));
		Assert.assertEquals(uploadRequestGroupService.findAll(john, john, null).size() - 1, groups.size());
		logger.debug(LinShareTestConstants.END_TEST);
	}

	@Test
	public void updateStatus() throws BusinessException {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		UploadRequest uploadRequest = initUploadRequest();
		UploadRequestGroup group = uploadRequestGroupService.create(john, john, uploadRequest, Lists.newArrayList(yoda), "This is a subject", "This is a body", false);
		Assert.assertEquals(UploadRequestStatus.ENABLED, group.getStatus());
		// Update upload request group status
		uploadRequestGroupService.updateStatus(john, john, group.getUuid(), UploadRequestStatus.CLOSED, false);
		Assert.assertEquals(UploadRequestStatus.CLOSED, group.getStatus());
		uploadRequestGroupService.updateStatus(john, john, group.getUuid(), UploadRequestStatus.ARCHIVED, true);
		Assert.assertEquals(UploadRequestStatus.ARCHIVED, group.getStatus());
		uploadRequestGroupService.updateStatus(john, john, group.getUuid(), UploadRequestStatus.DELETED, false);
		Assert.assertEquals(UploadRequestStatus.DELETED, group.getStatus());
		wiser.checkGeneratedMessages();
		logger.debug(LinShareTestConstants.END_TEST);
	}

	@Test
	public void updateStatusToCanceled() throws BusinessException {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		Date tomorrow = calendar.getTime();
		UploadRequest uploadRequest = initUploadRequest();
		uploadRequest.setActivationDate(tomorrow);
		UploadRequestGroup group = uploadRequestGroupService.create(john, john, uploadRequest, Lists.newArrayList(yoda), "This is a subject", "This is a body", false);
		Assert.assertEquals(UploadRequestStatus.CREATED, group.getStatus());
		uploadRequestGroupService.updateStatus(john, john, group.getUuid(), UploadRequestStatus.CANCELED, false);
		Assert.assertEquals(UploadRequestStatus.CANCELED, group.getStatus());
	}

	@Test
	public void updateStatusToClosedWithAlreadyClosedUploadRequest() throws BusinessException {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		uploadRequestGroupService.create(john, john, ure, Lists.newArrayList(yoda), "This is a subject",
				"This is a body", false);
		Assert.assertEquals(UploadRequestStatus.ENABLED, ure.getUploadRequestGroup().getStatus());
		uploadRequestService.updateStatus(john, john, ure.getUuid(), UploadRequestStatus.CLOSED, false);
		Assert.assertEquals(UploadRequestStatus.CLOSED, ure.getStatus());
		// Update upload request group status
		uploadRequestGroupService.updateStatus(john, john, ure.getUploadRequestGroup().getUuid(),
				UploadRequestStatus.CLOSED, false);
		Assert.assertEquals(UploadRequestStatus.CLOSED, ure.getUploadRequestGroup().getStatus());
		wiser.checkGeneratedMessages();
		logger.debug(LinShareTestConstants.END_TEST);
	}

	@Test
	public void update() throws BusinessException {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		UploadRequestGroup group = uploadRequestGroupService.find(john, john, ure.getUploadRequestGroup().getUuid());
		List<UploadRequest> uploadRequests = uploadRequestService.findAll(john, john, group, null);
		group.setUploadRequests(uploadRequests.stream().collect(Collectors.toSet()));
		group.setCanClose(false);
		group.setMaxFileCount(new Integer(5));
		UploadRequestGroup uploadRequestGroup = uploadRequestGroupService.update(john, john, group);
		Assert.assertEquals(false, uploadRequestGroup.getCanClose());
		Assert.assertEquals(new Integer(5), uploadRequestGroup.getMaxFileCount());
		UploadRequest uploadRequest = uploadRequests.get(0);
		Assert.assertEquals(false, uploadRequest.isCanClose());
		Assert.assertEquals(new Integer(5), uploadRequest.getMaxFileCount());
		wiser.checkGeneratedMessages();
		logger.debug(LinShareTestConstants.END_TEST);
	}

	@Test
	public void updateStatusToPurged() throws BusinessException {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		UploadRequestGroup uploadRequestGroup = uploadRequestGroupService.create(john, john, ure, Lists.newArrayList(yoda), "This is a subject",
				"This is a body", false);
		UploadRequest createdUploadRequest = uploadRequestGroup.getUploadRequests().iterator().next();
		uploadRequestGroupService.create(john, john, createdUploadRequest, Lists.newArrayList(yoda), "This is a subject",
				"This is a body", false);
		Assert.assertEquals(UploadRequestStatus.ENABLED, createdUploadRequest.getUploadRequestGroup().getStatus());
		// Update upload request group status
		uploadRequestGroupService.updateStatus(john, john, createdUploadRequest.getUploadRequestGroup().getUuid(),
				UploadRequestStatus.CLOSED, false);
		Assert.assertEquals(UploadRequestStatus.CLOSED, createdUploadRequest.getUploadRequestGroup().getStatus());
		uploadRequestGroupService.updateStatus(john, john, createdUploadRequest.getUploadRequestGroup().getUuid(),
				UploadRequestStatus.ARCHIVED, false);
		Assert.assertEquals(UploadRequestStatus.ARCHIVED, createdUploadRequest.getUploadRequestGroup().getStatus());
		// Try with copy false
		uploadRequestGroupService.updateStatus(john, john, createdUploadRequest.getUploadRequestGroup().getUuid(),
				UploadRequestStatus.PURGED, false);
		Assert.assertEquals("The given upload request has not a PURGED status", UploadRequestStatus.PURGED,
				createdUploadRequest.getUploadRequestGroup().getStatus());
		logger.info(LinShareTestConstants.END_TEST);
	}

	@Test
	public void updateStatusToPurgedAndCopy() throws BusinessException {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		UploadRequestGroup uploadRequestGroup = uploadRequestGroupService.create(john, john, ure, Lists.newArrayList(yoda), "This is a subject",
				"This is a body", false);
		UploadRequest createdUploadRequest = uploadRequestGroup.getUploadRequests().iterator().next();
		uploadRequestGroupService.create(john, john, createdUploadRequest, Lists.newArrayList(yoda),
				"This is a subject", "This is a body", false);
		Assert.assertEquals(UploadRequestStatus.ENABLED, createdUploadRequest.getUploadRequestGroup().getStatus());
		// Update upload request group status
		uploadRequestGroupService.updateStatus(john, john, createdUploadRequest.getUploadRequestGroup().getUuid(),
				UploadRequestStatus.CLOSED, false);
		Assert.assertEquals(UploadRequestStatus.CLOSED, createdUploadRequest.getUploadRequestGroup().getStatus());
		uploadRequestGroupService.updateStatus(john, john, createdUploadRequest.getUploadRequestGroup().getUuid(),
				UploadRequestStatus.ARCHIVED, false);
		Assert.assertEquals(UploadRequestStatus.ARCHIVED, createdUploadRequest.getUploadRequestGroup().getStatus());
		// Try with copy true
		uploadRequestGroupService.updateStatus(john, john, createdUploadRequest.getUploadRequestGroup().getUuid(),
				UploadRequestStatus.PURGED, true);
		Assert.assertEquals(UploadRequestStatus.PURGED, createdUploadRequest.getUploadRequestGroup().getStatus());
		logger.info(LinShareTestConstants.END_TEST);
	}

	// helpers
	private UploadRequest initUploadRequest() {
		UploadRequest uploadRequest = new UploadRequest();
		uploadRequest.setCanClose(true);
		uploadRequest.setMaxDepositSize((long) 100);
		uploadRequest.setMaxFileCount(new Integer(3));
		uploadRequest.setMaxFileSize((long) 50);
		uploadRequest.setStatus(UploadRequestStatus.CREATED);
		uploadRequest.setExpiryDate(new Date());
		uploadRequest.setSecured(false);
		uploadRequest.setCanEditExpiryDate(true);
		uploadRequest.setCanDelete(true);
		uploadRequest.setLocale("en");
		uploadRequest.setActivationDate(new Date());
		return uploadRequest;
	}
}
