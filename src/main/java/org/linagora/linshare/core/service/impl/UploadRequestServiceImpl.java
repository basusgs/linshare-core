/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2015-2018 LINAGORA
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
package org.linagora.linshare.core.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateUtils;
import org.linagora.linshare.core.business.service.DomainPermissionBusinessService;
import org.linagora.linshare.core.business.service.UploadRequestBusinessService;
import org.linagora.linshare.core.business.service.UploadRequestGroupBusinessService;
import org.linagora.linshare.core.business.service.UploadRequestHistoryBusinessService;
import org.linagora.linshare.core.business.service.UploadRequestTemplateBusinessService;
import org.linagora.linshare.core.domain.constants.AuditLogEntryType;
import org.linagora.linshare.core.domain.constants.LogAction;
import org.linagora.linshare.core.domain.constants.UploadRequestStatus;
import org.linagora.linshare.core.domain.entities.AbstractDomain;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.Contact;
import org.linagora.linshare.core.domain.entities.UploadRequest;
import org.linagora.linshare.core.domain.entities.UploadRequestGroup;
import org.linagora.linshare.core.domain.entities.UploadRequestHistory;
import org.linagora.linshare.core.domain.entities.UploadRequestTemplate;
import org.linagora.linshare.core.domain.entities.UploadRequestUrl;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.domain.objects.MailContainerWithRecipient;
import org.linagora.linshare.core.exception.BusinessErrorCode;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.notifications.context.EmailContext;
import org.linagora.linshare.core.notifications.context.UploadRequestActivationEmailContext;
import org.linagora.linshare.core.notifications.context.UploadRequestCloseByOwnerEmailContext;
import org.linagora.linshare.core.notifications.context.UploadRequestClosedByRecipientEmailContext;
import org.linagora.linshare.core.notifications.context.UploadRequestCreatedEmailContext;
import org.linagora.linshare.core.notifications.service.MailBuildingService;
import org.linagora.linshare.core.rac.UploadRequestGroupResourceAccessControl;
import org.linagora.linshare.core.rac.UploadRequestResourceAccessControl;
import org.linagora.linshare.core.rac.UploadRequestTemplateResourceAccessControl;
import org.linagora.linshare.core.repository.AccountRepository;
import org.linagora.linshare.core.service.LogEntryService;
import org.linagora.linshare.core.service.NotifierService;
import org.linagora.linshare.core.service.UploadRequestService;
import org.linagora.linshare.core.service.UploadRequestUrlService;
import org.linagora.linshare.mongo.entities.logs.UploadRequestAuditLogEntry;
import org.linagora.linshare.mongo.entities.mto.AccountMto;
import org.linagora.linshare.mongo.entities.mto.UploadRequestMto;

import com.google.common.collect.Lists;

public class UploadRequestServiceImpl extends GenericServiceImpl<Account, UploadRequest> implements UploadRequestService {

	private final AccountRepository<Account> accountRepository;
	private final UploadRequestBusinessService uploadRequestBusinessService;
	private final UploadRequestGroupBusinessService uploadRequestGroupBusinessService;
	private final UploadRequestHistoryBusinessService uploadRequestHistoryBusinessService;
	private final UploadRequestTemplateBusinessService uploadRequestTemplateBusinessService;
	private final UploadRequestUrlService uploadRequestUrlService;
	private final DomainPermissionBusinessService domainPermissionBusinessService;
	private final MailBuildingService mailBuildingService;
	private final NotifierService notifierService;
	private final UploadRequestTemplateResourceAccessControl templateRac;
	private final UploadRequestGroupResourceAccessControl groupRac;
	private final LogEntryService logEntryService;

	public UploadRequestServiceImpl(
			final AccountRepository<Account> accountRepository,
			final UploadRequestBusinessService uploadRequestBusinessService,
			final UploadRequestGroupBusinessService uploadRequestGroupBusinessService,
			final UploadRequestHistoryBusinessService uploadRequestHistoryBusinessService,
			final UploadRequestTemplateBusinessService uploadRequestTemplateBusinessService,
			final UploadRequestUrlService uploadRequestUrlService,
			final DomainPermissionBusinessService domainPermissionBusinessService,
			final MailBuildingService mailBuildingService,
			final NotifierService notifierService,
			final UploadRequestResourceAccessControl rac,
			final UploadRequestTemplateResourceAccessControl templateRac,
			final UploadRequestGroupResourceAccessControl groupRac,
			final LogEntryService logEntryService
			) {
		super(rac);
		this.accountRepository = accountRepository;
		this.uploadRequestBusinessService = uploadRequestBusinessService;
		this.uploadRequestGroupBusinessService = uploadRequestGroupBusinessService;
		this.uploadRequestHistoryBusinessService = uploadRequestHistoryBusinessService;
		this.uploadRequestTemplateBusinessService = uploadRequestTemplateBusinessService;
		this.uploadRequestUrlService = uploadRequestUrlService;
		this.domainPermissionBusinessService = domainPermissionBusinessService;
		this.mailBuildingService = mailBuildingService;
		this.notifierService = notifierService;
		this.templateRac = templateRac;
		this.groupRac = groupRac;
		this.logEntryService = logEntryService;
	}

	@Override
	public List<UploadRequest> findAllRequest(Account actor, Account owner, List<UploadRequestStatus> statusList) {
		preChecks(actor, owner);
		checkListPermission(actor, owner, UploadRequest.class, BusinessErrorCode.UPLOAD_REQUEST_FORBIDDEN, null);
		return uploadRequestBusinessService.findAll((User) owner, statusList);
	}

	@Override
	public UploadRequest findRequestByUuid(Account actor, Account owner, String uuid)
			throws BusinessException {
		UploadRequest ret = uploadRequestBusinessService.findByUuid(uuid);
		if (ret == null) {
			throw new BusinessException(BusinessErrorCode.UPLOAD_REQUEST_NOT_FOUND, "Can not find upload request with uuid : " + uuid);
		}
		if (owner == null) {
			owner = ret.getUploadRequestGroup().getOwner();
		}
		checkReadPermission(actor, owner, UploadRequest.class, BusinessErrorCode.UPLOAD_REQUEST_FORBIDDEN, ret);
		return ret;
	}

	@Override
	public UploadRequest updateStatus(Account authUser, Account actor, String uuid, UploadRequestStatus status)
			throws BusinessException {
		Validate.notNull(authUser, "Actor must be set.");
		Validate.notNull(actor, "Owner must be set.");
		UploadRequest req = findRequestByUuid(authUser, actor, uuid);
		checkUpdatePermission(authUser, actor, UploadRequest.class, BusinessErrorCode.UPLOAD_REQUEST_FORBIDDEN, req);
		if (!req.getUploadRequestGroup().getRestricted() && !status.equals(req.getUploadRequestGroup().getStatus())) {
			// if is grouped mode and the new status is not equal 
			// to the current status of uploadRequestGroup 
			throw new BusinessException(BusinessErrorCode.FORBIDDEN, "You can not edit an uploadRequest : " + req.getUuid()); 
		}
		UploadRequestAuditLogEntry log = new UploadRequestAuditLogEntry(new AccountMto(authUser), new AccountMto(actor),
				LogAction.UPDATE, AuditLogEntryType.UPLOAD_REQUEST, req.getUuid(), req);
		req = uploadRequestBusinessService.updateStatus(req, status);
		sendNotification(req, actor);
		log.setResourceUpdated(new UploadRequestMto(req));
		logEntryService.insert(log);
		return req;
	}

	private void sendNotification(UploadRequest req, Account actor) {
		List<MailContainerWithRecipient> mails = Lists.newArrayList();
		if (UploadRequestStatus.STATUS_ENABLED.equals(req.getStatus())) {
			mails.add(mailBuildingService.build(new UploadRequestActivationEmailContext((User) actor, req)));
		} else if (req.getEnableNotification()) {
			if (UploadRequestStatus.STATUS_CANCELED.equals(req.getStatus())) {
				// TODO return context
			} else if (UploadRequestStatus.STATUS_CLOSED.equals(req.getStatus())) {
				for (UploadRequestUrl urUrl : req.getUploadRequestURLs()) {
					mails.add(mailBuildingService.build(new UploadRequestCloseByOwnerEmailContext((User) actor, urUrl, req)));
				}
			} else if (UploadRequestStatus.STATUS_ARCHIVED.equals(req.getStatus())) {
				// TODO return context
			} else if (UploadRequestStatus.STATUS_DELETED.equals(req.getStatus())) {
				// TODO return context
			}
		}
		notifierService.sendNotification(mails);
		mails.clear();
	}

	@Override
	public UploadRequest update(Account actor, Account owner, String uuid, UploadRequest object)
			throws BusinessException {
		Validate.notNull(actor, "Actor must be set.");
		Validate.notNull(owner, "Owner must be set.");
		Validate.notEmpty(uuid, "Uuid must be set.");
		Validate.notNull(object, "Object must be set.");
		UploadRequest uploadRequest = findRequestByUuid(actor, owner, uuid);
		UploadRequestAuditLogEntry log = new UploadRequestAuditLogEntry(new AccountMto(actor), new AccountMto(owner),
				LogAction.UPDATE, AuditLogEntryType.UPLOAD_REQUEST, uploadRequest.getUuid(), uploadRequest);
		checkUpdatePermission(actor, owner, UploadRequest.class, BusinessErrorCode.UPLOAD_REQUEST_FORBIDDEN,
				uploadRequest);
		if (!uploadRequest.getUploadRequestGroup().getRestricted()) {
			throw new BusinessException(BusinessErrorCode.UPLOAD_REQUEST_NOT_UPDATABLE_GROUP_MODE,
					"Connot update upload request in grouped mode, try to update the upload request group");
		}
		UploadRequest res = uploadRequestBusinessService.update(uploadRequest, object);
		log.setResourceUpdated(new UploadRequestMto(res));
		logEntryService.insert(log);
		return res;
	}

	@Override
	public UploadRequest updateRequest(Account actor, Account owner, UploadRequest req) throws BusinessException {
		checkUpdatePermission(actor, owner, UploadRequest.class, BusinessErrorCode.UPLOAD_REQUEST_FORBIDDEN, req);
		return uploadRequestBusinessService.update(req);
	}

	@Override
	public UploadRequest closeRequestByRecipient(UploadRequestUrl url)
			throws BusinessException {
		Account actor = accountRepository.getUploadRequestSystemAccount();
		UploadRequest req = url.getUploadRequest();
		if (req.getStatus().equals(UploadRequestStatus.STATUS_CLOSED)) {
			throw new BusinessException(BusinessErrorCode.FORBIDDEN,
					"Closing an already closed upload request url : "
							+ req.getUuid());
		}
		UploadRequestAuditLogEntry log = new UploadRequestAuditLogEntry(new AccountMto(actor),
				new AccountMto(req.getUploadRequestGroup().getOwner()), LogAction.UPDATE, AuditLogEntryType.UPLOAD_REQUEST, req.getUuid(), req);
		checkUpdatePermission(actor, req.getUploadRequestGroup().getOwner(), UploadRequest.class, BusinessErrorCode.UPLOAD_REQUEST_FORBIDDEN, req);
		req.updateStatus(UploadRequestStatus.STATUS_CLOSED);
		UploadRequest update = updateRequest(actor, actor, req);
		EmailContext ctx = new UploadRequestClosedByRecipientEmailContext((User)req.getUploadRequestGroup().getOwner(), req, url);
		MailContainerWithRecipient mail = mailBuildingService.build(ctx);
		notifierService.sendNotification(mail);
		log.setResourceUpdated(new UploadRequestMto(update));
		logEntryService.insert(log);
		return update;
	}

	@Override
	public UploadRequest deleteRequest(Account actor, Account owner, String uuid)
			throws BusinessException {
		return updateStatus(actor, owner, uuid, UploadRequestStatus.STATUS_DELETED);
	}

	@Override
	public UploadRequestGroup updateRequestGroup(Account actor,
			Account owner, UploadRequestGroup group) throws BusinessException {
		preChecks(actor, owner);
		groupRac.checkUpdatePermission(actor, group.getUploadRequests().iterator().next().getUploadRequestGroup().getOwner(), UploadRequestGroup.class, BusinessErrorCode.UPLOAD_REQUEST_FORBIDDEN, group);
		return uploadRequestGroupBusinessService.update(group);
	}

	@Override
	public void deleteRequestGroup(Account actor, Account owner, UploadRequestGroup group)
			throws BusinessException {
		preChecks(actor, owner);
		groupRac.checkDeletePermission(actor, group.getUploadRequests().iterator().next().getUploadRequestGroup().getOwner(), UploadRequestGroup.class, BusinessErrorCode.UPLOAD_REQUEST_FORBIDDEN, group);
		uploadRequestGroupBusinessService.delete(group);
	}

	@Override
	public Set<UploadRequestHistory> findAllRequestHistory(Account actor, Account owner,
			String uploadRequestUuid) throws BusinessException {
		UploadRequest request = findRequestByUuid(actor, owner, uploadRequestUuid);
		if (!domainPermissionBusinessService.isAdminForThisUploadRequest(actor,
				request)) {
			throw new BusinessException(
					BusinessErrorCode.UPLOAD_REQUEST_FORBIDDEN,
					"Upload request history search forbidden");
		}
		return request.getUploadRequestHistory();
	}

	@Override
	public Set<UploadRequest> findAll(Account actor,
			List<UploadRequestStatus> status, Date afterDate, Date beforeDate)
			throws BusinessException {
		if (!actor.hasSuperAdminRole()) {
			throw new BusinessException(
					BusinessErrorCode.UPLOAD_REQUEST_FORBIDDEN,
					"Upload request history search forbidden");
		}
		if (afterDate == null) {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MONTH, -1);
			afterDate = c.getTime();
		}
		if (beforeDate == null) {
			beforeDate = new Date();
		}
		if (afterDate.after(beforeDate)) {
			throw new BusinessException(BusinessErrorCode.BAD_REQUEST,
					"Min date limit after max date limit");
		}
		List<AbstractDomain> myAdministredDomains = domainPermissionBusinessService
				.getMyAdministredDomains(actor);
		return new HashSet<UploadRequest>(uploadRequestBusinessService.findAll(
				myAdministredDomains, status, afterDate, beforeDate));
	}

	@Override
	public UploadRequestHistory findRequestHistoryByUuid(Account actor,
			String uuid) {
		return uploadRequestHistoryBusinessService.findByUuid(uuid);
	}

	@Override
	public UploadRequestHistory createRequestHistory(Account actor,
			UploadRequestHistory history) throws BusinessException {
		return uploadRequestHistoryBusinessService.create(history);
	}

	@Override
	public UploadRequestHistory updateRequestHistory(Account actor,
			UploadRequestHistory history) throws BusinessException {
		return uploadRequestHistoryBusinessService.update(history);
	}

	@Override
	public void deleteRequestHistory(Account actor, UploadRequestHistory history)
			throws BusinessException {
		uploadRequestHistoryBusinessService.delete(history);
	}

	@Override
	public UploadRequestTemplate findTemplateByUuid(Account actor, Account owner,
			String uuid) throws BusinessException {
		Validate.notEmpty(uuid, "Template uuid must be set.");
		preChecks(actor, owner);
		UploadRequestTemplate ret = uploadRequestTemplateBusinessService.findByUuid(uuid);
		if (ret == null) {
			throw new BusinessException(BusinessErrorCode.NO_SUCH_ELEMENT, "UploadRequestTemplate with uuid: " + uuid + " not found.");
		}
		templateRac.checkCreatePermission(actor, ret.getOwner(), UploadRequestTemplate.class,
				BusinessErrorCode.UPLOAD_REQUEST_TEMPLATE_FORBIDDEN, ret);
		return ret;
	}

	@Override
	public UploadRequestTemplate createTemplate(Account actor, Account owner,
			UploadRequestTemplate template) throws BusinessException {
		UploadRequestTemplate temp = uploadRequestTemplateBusinessService.create(actor, template);
		templateRac.checkCreatePermission(actor, temp.getOwner(), UploadRequestTemplate.class, BusinessErrorCode.UPLOAD_REQUEST_TEMPLATE_FORBIDDEN, temp);
		return temp;
	}

	@Override
	public UploadRequestTemplate updateTemplate(Account actor, Account owner, String uuid,
			UploadRequestTemplate template) throws BusinessException {
		Validate.notEmpty(uuid, "Template uuid must be set.");
		Validate.notNull(template, "Template must be set.");
		UploadRequestTemplate temp = findTemplateByUuid(actor, owner, uuid);
		templateRac.checkUpdatePermission(actor, temp.getOwner(), UploadRequestTemplate.class, BusinessErrorCode.UPLOAD_REQUEST_TEMPLATE_FORBIDDEN, temp);
		return uploadRequestTemplateBusinessService.update(temp, template);
	}

	@Override
	public UploadRequestTemplate deleteTemplate(Account actor, Account owner, String uuid) throws BusinessException {
		Validate.notEmpty(uuid, "Template uuid must be set.");
		UploadRequestTemplate template = findTemplateByUuid(actor, owner, uuid);
		templateRac.checkDeletePermission(actor, template.getOwner(), UploadRequestTemplate.class, BusinessErrorCode.UPLOAD_REQUEST_TEMPLATE_FORBIDDEN, template);
		uploadRequestTemplateBusinessService.delete(template);
		return template;
	}

	@Override
	public List<String> findOutdatedRequests(Account actor) {
		checkActorPermission(actor);
		return uploadRequestBusinessService.findOutdatedRequests();
	}

	@Override
	public List<String> findUnabledRequests(Account actor) {
		checkActorPermission(actor);
		return uploadRequestBusinessService.findUnabledRequests();
	}

	@Override
	public List<String> findAllRequestsToBeNotified(Account actor) {
		checkActorPermission(actor);
		return uploadRequestBusinessService.findAllRequestsToBeNotified();
	}

	private void checkActorPermission(Account actor) {
		if (!actor.hasAllRights()) {
			throw new BusinessException(BusinessErrorCode.FORBIDDEN, "You have no rights to access this service.");
		}
	}

	private void checkStatusPermission(UploadRequestStatus status) {
		if ((UploadRequestStatus.STATUS_CREATED.equals(status) != true) &&(UploadRequestStatus.STATUS_ENABLED.equals(status) != true)) {
			throw new BusinessException(BusinessErrorCode.FORBIDDEN, "You have no rights to add new recipient");
		}
	}

	@Override
	public UploadRequest addNewRecipient(User authUser, User actor, UploadRequestGroup uploadRequestGroup, Contact contact) {
		checkStatusPermission(uploadRequestGroup.getStatus());
		groupRac.checkUpdatePermission(authUser, actor, UploadRequestGroup.class, BusinessErrorCode.UPLOAD_REQUEST_FORBIDDEN, uploadRequestGroup);
		UploadRequest uploadRequest = initLocalConfig(uploadRequestGroup);
		UploadRequestUrl uploadRequestUrl;
		if (uploadRequestGroup.getRestricted()) {
			uploadRequest = uploadRequestBusinessService.create(uploadRequest);
			uploadRequestUrl = uploadRequestUrlService.create(uploadRequest, contact);
		} else {
			uploadRequest = uploadRequestGroup.getUploadRequests().iterator().next();
			uploadRequestUrl = uploadRequestUrlService.create(uploadRequest, contact);
		}
		if (!DateUtils.isSameDay(uploadRequest.getActivationDate(), uploadRequest.getCreationDate()) &&
				uploadRequest.getActivationDate().after(new Date())) {
			if (uploadRequest.getEnableNotification()) {
				UploadRequestCreatedEmailContext context = new UploadRequestCreatedEmailContext((User) uploadRequest.getUploadRequestGroup().getOwner(),
						uploadRequestUrl, uploadRequest);
				notifierService.sendNotification(mailBuildingService.build(context));
			}
		} else {
			UploadRequestActivationEmailContext mailContext = new UploadRequestActivationEmailContext(
					(User) uploadRequestGroup.getOwner(), uploadRequest, uploadRequestUrl);
			MailContainerWithRecipient mail = mailBuildingService.build(mailContext);
			notifierService.sendNotification(mail);
		}
		return uploadRequest;
	}

	protected UploadRequest initLocalConfig (UploadRequestGroup urg) {
		UploadRequest uploadRequest = new UploadRequest();
		if (urg.getActivationDate().before(new Date())) {
			uploadRequest.setActivationDate(new Date());
		} else {
			uploadRequest.setActivationDate(urg.getActivationDate());
		}
		uploadRequest.setCanDelete(urg.getCanDelete());
		uploadRequest.setCanClose(urg.getCanClose());
		uploadRequest.setCanEditExpiryDate(urg.getCanEditExpiryDate());
		uploadRequest.setLocale(urg.getLocale());
		uploadRequest.setSecured(urg.isSecured());
		uploadRequest.setEnableNotification(urg.getEnableNotification());
		uploadRequest.setExpiryDate(urg.getExpiryDate());
		uploadRequest.setNotificationDate(urg.getNotificationDate());
		uploadRequest.setMaxFileCount(urg.getMaxFileCount());
		uploadRequest.setMaxDepositSize(urg.getMaxDepositSize());
		uploadRequest.setMaxFileSize(urg.getMaxFileSize());
		uploadRequest.setUploadRequestGroup(urg);
		uploadRequest.setStatus(urg.getStatus());
		return uploadRequest;
	}

	@Override
	public List<UploadRequest> findAllRequestsByGroup(User authUser, User actor, String groupUuid, List<UploadRequestStatus> statusList) {
		preChecks(authUser, actor);
		checkListPermission(authUser, actor, UploadRequest.class, BusinessErrorCode.UPLOAD_REQUEST_FORBIDDEN, null);
		UploadRequestGroup uploadRequestGroup = uploadRequestGroupBusinessService.findByUuid(groupUuid);
		Validate.notNull(uploadRequestGroup);
		return uploadRequestBusinessService.findByGroup(uploadRequestGroup, statusList);
	}
}
