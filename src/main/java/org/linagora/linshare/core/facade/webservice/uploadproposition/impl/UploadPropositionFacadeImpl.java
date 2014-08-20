/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2014 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009–2014. Contribute to
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

package org.linagora.linshare.core.facade.webservice.uploadproposition.impl;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.linagora.linshare.core.domain.constants.UploadPropositionActionType;
import org.linagora.linshare.core.domain.constants.UploadPropositionMatchType;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.webservice.uploadproposition.UploadPropositionFacade;
import org.linagora.linshare.core.facade.webservice.uploadproposition.dto.UploadPropositionActionDto;
import org.linagora.linshare.core.facade.webservice.uploadproposition.dto.UploadPropositionDto;
import org.linagora.linshare.core.facade.webservice.uploadproposition.dto.UploadPropositionFilterDto;
import org.linagora.linshare.core.service.AccountService;
import org.linagora.linshare.core.service.FunctionalityReadOnlyService;
import org.linagora.linshare.core.service.UploadPropositionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class UploadPropositionFacadeImpl extends
		UploadPropositionGenericFacadeImpl implements UploadPropositionFacade {

	private static final Logger logger = LoggerFactory
			.getLogger(UploadPropositionFacadeImpl.class);

	private final UploadPropositionService uploadPropositionService;

	public UploadPropositionFacadeImpl(AccountService accountService,
			UploadPropositionService uploadPropositionService,
			FunctionalityReadOnlyService functionalityService) {
		super(accountService, functionalityService);
		this.uploadPropositionService = uploadPropositionService;
	}

	@Override
	public List<UploadPropositionFilterDto> findAll() throws BusinessException {
		this.checkAuthentication();
		List<UploadPropositionFilterDto> filters = Lists.newArrayList();
		filters.add(addDefaultFilter());
		return filters;
	}

	private UploadPropositionFilterDto addDefaultFilter() {
		UploadPropositionActionDto action = new UploadPropositionActionDto(
				"ee1bf0ab-21ad-4a69-914d-d792eb2b36d7",
				UploadPropositionActionType.ACCEPT, null);
		UploadPropositionFilterDto filter = new UploadPropositionFilterDto(
				"5724946a-eebe-450b-bb84-0d8af480f3f6", "default filter 1",
				UploadPropositionMatchType.TRUE);
		filter.getUploadPropositionActions().add(action);
		return filter;
	}

	@Override
	public void checkIfValidRecipient(String userMail, String userDomain)
			throws BusinessException {
		Validate.notEmpty(userMail, "User user mail is required.");
		User actor = checkAuthentication();
		uploadPropositionService.checkIfValidRecipient(actor, userMail,
				userDomain);
	}

	@Override
	public void create(UploadPropositionDto dto) throws BusinessException {
		this.checkAuthentication();
		logger.debug(dto.toString());
		UploadPropositionActionType actionType = UploadPropositionActionType
				.fromString(dto.getAction());
		uploadPropositionService.create(dto.toEntity(dto), actionType);
	}
}
