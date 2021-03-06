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

package org.linagora.linshare.core.rac.impl;

import org.linagora.linshare.core.domain.constants.TechnicalAccountPermissionType;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.domain.entities.WorkGroup;
import org.linagora.linshare.core.domain.entities.WorkgroupMember;
import org.linagora.linshare.core.rac.ThreadMemberResourceAccessControl;
import org.linagora.linshare.core.repository.ThreadMemberRepository;
import org.linagora.linshare.core.service.FunctionalityReadOnlyService;

public class ThreadMemberResourceAccessControlImpl extends
		AbstractResourceAccessControlImpl<Account, Account, WorkgroupMember>
		implements ThreadMemberResourceAccessControl {

	private final ThreadMemberRepository threadMemberRepository;

	public ThreadMemberResourceAccessControlImpl(
			final FunctionalityReadOnlyService functionalityService,
			final ThreadMemberRepository threadMemberRepository) {
		super(functionalityService);
		this.threadMemberRepository = threadMemberRepository;
	}

	@Override
	protected boolean hasReadPermission(Account authUser, Account actor,
			WorkgroupMember entry, Object... opt) {
		if (authUser.hasAllRights()) {
			return true;
		}
		if (authUser.hasDelegationRole()) {
			return hasPermission(authUser,
					TechnicalAccountPermissionType.THREAD_MEMBERS_GET);
		}
		return threadMemberRepository.findUserThreadMember(entry.getThread(),
				(User) actor) != null;
	}

	@Override
	protected boolean hasListPermission(Account authUser, Account actor,
			WorkgroupMember entry, Object... opt) {
		if (authUser.hasAllRights()) {
			return true;
		}
		if (authUser.hasDelegationRole()) {
			return hasPermission(authUser,
					TechnicalAccountPermissionType.THREAD_MEMBERS_LIST);
		}
		if (opt.length > 0 && opt[0] instanceof WorkGroup) {
			return threadMemberRepository.findUserThreadMember((WorkGroup) opt[0],
					(User) actor) != null;
		}
		return false;
	}

	@Override
	protected boolean hasDeletePermission(Account authUser, Account actor,
			WorkgroupMember entry, Object... opt) {
		if (authUser.hasAllRights()) {
			return true;
		}
		if (authUser.hasDelegationRole()) {
			return hasPermission(authUser,
					TechnicalAccountPermissionType.THREAD_MEMBERS_DELETE);
		}
		WorkgroupMember member = threadMemberRepository.findUserThreadMember(
				entry.getThread(), (User) actor);
		if (member != null) {
			return member.getAdmin();
		}
		return false;
	}

	@Override
	protected boolean hasCreatePermission(Account authUser, Account actor,
			WorkgroupMember entry, Object... opt) {
		if (authUser.hasAllRights()) {
			return true;
		}
		if (authUser.hasDelegationRole()) {
			return hasPermission(authUser,
					TechnicalAccountPermissionType.THREAD_MEMBERS_CREATE);
		}
		// entry is the object to be created. Can not be used to check if the
		// current owner is admin.
		if (opt.length > 0 && opt[0] instanceof WorkGroup) {
			WorkgroupMember member = threadMemberRepository.findUserThreadMember(
					(WorkGroup) opt[0], (User) actor);
			if (member != null) {
				return member.getAdmin();
			}
		}
		return false;
	}

	@Override
	protected boolean hasUpdatePermission(Account authUser, Account actor,
			WorkgroupMember entry, Object... opt) {
		if (authUser.hasAllRights()) {
			return true;
		}
		if (authUser.hasDelegationRole()) {
			return hasPermission(authUser,
					TechnicalAccountPermissionType.THREAD_MEMBERS_UPDATE);
		}
		WorkgroupMember member = threadMemberRepository.findUserThreadMember(
				entry.getThread(), (User) actor);
		if (member != null) {
			return member.getAdmin();
		}
		return false;
	}

	@Override
	protected String getEntryRepresentation(WorkgroupMember entry) {
		return '(' + entry.getThread().getLsUuid() + ':'
				+ entry.getUser().getLsUuid() + ')';
	}

	@Override
	protected String getTargetedAccountRepresentation(Account targetedAccount) {
		return targetedAccount.getAccountRepresentation();
	}

	@Override
	protected Account getOwner(WorkgroupMember entry, Object... opt) {
		return entry.getUser().getOwner();
	}
}
