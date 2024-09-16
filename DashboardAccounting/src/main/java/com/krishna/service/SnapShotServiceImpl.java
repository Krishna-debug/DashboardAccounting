package com.krishna.service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.hibernate.hql.spi.id.AbstractMultiTableBulkIdStrategyImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import com.krishna.controller.FeignLegacyInterface;
import com.krishna.domain.ReserveSnapShot;
import com.krishna.domain.Margin.ProjectSnapshots;
import com.krishna.repository.ProjectSnapshotRepository;
import com.krishna.repository.ReserveSnapShotRepository;
import com.krishna.service.util.ConsolidatedService;

@Service
public class SnapShotServiceImpl implements SnapShotService {
	
	@Autowired
	FeignLegacyInterface legacyInterface;
	
	@Autowired
	MailService mailService;
	
	@Autowired
	ConsolidatedService consolidatedService;
	
	@Autowired
	ReserveSnapShotRepository reserveSnapShotRepository;
	
	@Autowired
	ProjectSnapshotRepository snapshotRepository;
	
	@Autowired
	ProjectInvoiceService projectInvoiceService;

	@Value("${com.oodles.dashbordAdmin.email}")
	private String dashboardAdminMail;

	Logger log = LoggerFactory.getLogger(SnapShotServiceImpl.class);

	
	@Override
	public List<Map<Object,Object>> getBuWiseMarginSnapShot(String accessToken, Long to, Long from,String businessVertical){
		List<Map<Object,Object>> dataMargins = new ArrayList<>();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(to));
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dateToFind = calendar.getTime();		
		String formattedDate=formatter.format(dateToFind);

		try {
		dateToFind=formatter.parse(formattedDate);
		}
		catch (Exception e) {
			log.error("---------Unable to parse Date----- "+dateToFind);
		}
		
		calendar.setTime(new Date(from));
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date preDateToFind = calendar.getTime();
		formattedDate=formatter.format(preDateToFind);
		try {
			preDateToFind=formatter.parse(formattedDate);
		}
		catch (Exception e) {
			log.error("--------Unable to parse Date------ "+preDateToFind);
		}
		
		
		Map<Object, Object> buData = new HashMap<>();
		buData.put("buName", businessVertical);
		List<Object> projectsListData = new ArrayList<>();
		List<Map<String,Object>>projects=(List<Map<String, Object>>) legacyInterface.getBuProjectsForsnapshots(accessToken,businessVertical).get("data");
		int projectListSize = projects.size();
		Double buTodayIC = 0D;
		Double buPreIC = 0D;
		Double buTodayDC = 0D;
		Double buPreDC = 0D;
		Double buTodayRevenue = 0D;
		Double buPreRevenue = 0D;
		Double buCurrentMargin = 0D;
		Double buPriMargin = 0D;
		for (int j = 0; j < projectListSize; j++) {
			Map<String, Object> project = (Map<String, Object>) projects.get(j);
			Map<Object, Object> projectData = new HashMap<>();

			projectData.put("projectName", project.get("name"));
			projectData.put("proId", project.get("id"));
			Double marginDiff = 0D;
			Map<Object, Object> yesterdayMap = new HashMap<>();
			Map<Object, Object> toDayMap = new HashMap<>();
			ProjectSnapshots todaySnap=getSnapShotByProjectId(Long.parseLong(project.get("id").toString()), dateToFind);
			ProjectSnapshots preSnap = getSnapShotByProjectId(Long.parseLong(project.get("id").toString()), preDateToFind);

			if(todaySnap!=null) {
				buTodayIC=	buTodayIC+todaySnap.getIndirectCost();
				buTodayDC=  buTodayDC+todaySnap.getDirectCost();
				buTodayRevenue = buTodayRevenue + todaySnap.getInvoiceAmountInRupees();
			}
			if(preSnap!=null) {
				buPreIC = buPreIC + preSnap.getIndirectCost();
				buPreDC = buPreDC + preSnap.getDirectCost();
				buPreRevenue = buPreRevenue + preSnap.getInvoiceAmountInRupees();
			}
			if(todaySnap!=null && preSnap!=null) {
				if(todaySnap.isChanged()) {
					yesterdayMap.put("margin", preSnap.getMargin());
					yesterdayMap.put("projectCost", preSnap.getIndirectCost()+preSnap.getDirectCost());
					yesterdayMap.put("indirectCost", preSnap.getIndirectCost());
					yesterdayMap.put("directCost", preSnap.getDirectCost());
					yesterdayMap.put("revenue", preSnap.getInvoiceAmountInRupees());
					marginDiff = todaySnap.getMargin() - preSnap.getMargin();
					toDayMap.put("margin", todaySnap.getMargin());
					toDayMap.put("projectCost", todaySnap.getIndirectCost() + todaySnap.getDirectCost());
					toDayMap.put("indirectCost", todaySnap.getIndirectCost());
					toDayMap.put("directCost", todaySnap.getDirectCost());
					toDayMap.put("revenue", todaySnap.getInvoiceAmountInRupees());
					projectData.put("marginDiff", Math.round((marginDiff) * 100.00) / 100.00);
					projectData.put("isIndirectCostChanged", false);
					projectData.put("isDcChanged", false);
					projectData.put("isRevenueChanged", false);
					if(preSnap.getIndirectCost()!=todaySnap.getIndirectCost())
						projectData.put("isIndirectCostChanged", true);
					if(preSnap.getInvoiceAmountInRupees()!=todaySnap.getInvoiceAmountInRupees())
						projectData.put("isRevenueChanged", true);
					if(preSnap.getDirectCost()!=todaySnap.getDirectCost())
						projectData.put("isDirectCostChanged", true);
					projectData.put(dateToFind.getTime(), toDayMap);
					projectData.put(preDateToFind.getTime(), yesterdayMap);
					projectsListData.add(projectData);
				}
			}
		}
		
		if (!projectsListData.isEmpty()) {
			buCurrentMargin = buTodayRevenue - (buTodayDC + buTodayIC);
			if (buPreRevenue != 0)
				buPriMargin = buPreRevenue - (buPreDC + buPreIC);
			else
				buPriMargin = 0D;
			Map<Object, Object> buTodayMargin = new HashMap<>();
			buTodayMargin.put("margin", Math.round((buCurrentMargin) * 100.00) / 100.00);
			buTodayMargin.put("indirectCost", Math.round((buTodayIC) * 100.00) / 100.00);
			buTodayMargin.put("directCost", Math.round((buTodayDC) * 100.00) / 100.00);
			buTodayMargin.put("revenue", Math.round((buTodayRevenue) * 100.00) / 100.00);
			Map<Object, Object> buPreMargin = new HashMap<>();
			buPreMargin.put("margin", Math.round((buPriMargin) * 100.00) / 100.00);
			buPreMargin.put("indirectCost", Math.round((buPreIC) * 100.00) / 100.00);
			buPreMargin.put("directCost", Math.round((buPreDC) * 100.00) / 100.00);
			buPreMargin.put("revenue", Math.round((buPreRevenue) * 100.00) / 100.00);
			if (!buTodayIC .equals(buPreIC))
				buData.put("isIndirectCostChanged", true);
			else
				buData.put("isIndirectCostChanged", false);
			if (!buTodayDC .equals(buPreDC))
				buData.put("isDirectCostChanged", true);
			else
				buData.put("isDirectCostChanged", false);
			if (!buTodayRevenue.equals(buPreRevenue) )
				buData.put("isRevenueChanged", true);
			else
				buData.put("isRevenueChanged", false);
			buData.put(dateToFind.getTime(), buTodayMargin);
			buData.put(preDateToFind.getTime(), buPreMargin);
			buData.put("marginDiff", Math.round((buCurrentMargin - buPriMargin) * 100.00) / 100.00);
			buData.put("projects", projectsListData);
			dataMargins.add(buData);
		}
		return dataMargins;
	}
	
	@Override
	public Map<Object,Object> getBuWiseReserveSnapShot(String accessToken, Long to, Long from,String businessVertical) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(to));
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dateToFind = calendar.getTime();		
		String formattedDate=formatter.format(dateToFind);

		try {
		dateToFind=formatter.parse(formattedDate);
		}
		catch (Exception e) {
			log.error("---------Unable to parse Date----- "+dateToFind);
		}
		
		calendar.setTime(new Date(from));
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date preDateToFind = calendar.getTime();
		formattedDate=formatter.format(preDateToFind);
		try {
			preDateToFind=formatter.parse(formattedDate);
		}
		catch (Exception e) {
			log.error("--------Unable to parse Date------ "+preDateToFind);
		}
			Map<Object, Object> projectData = new HashMap<>();
			Map<Object, Object> yesterdayMap = new HashMap<>();
			Map<Object, Object> toDayMap = new HashMap<>();
			ReserveSnapShot todaySnap=getSnapShot(businessVertical, dateToFind);
			ReserveSnapShot preSnap =getSnapShot(businessVertical, preDateToFind);

			if(todaySnap!=null && preSnap!=null) {
				if(todaySnap.isChanged()) {
					yesterdayMap.put("totalMargin", preSnap.getTotalMargin());
					yesterdayMap.put("totalMarginPerc", preSnap.getTotalMarginPerc());

					yesterdayMap.put("ytdDisputed", preSnap.getYtdDisputed());
					yesterdayMap.put("ytdDisputedPerc", preSnap.getYtdDisputedPerc());

					yesterdayMap.put("netMargin", preSnap.getNetMargin());
					yesterdayMap.put("netMarginPerc", preSnap.getNetMarginPerc());

					yesterdayMap.put("monthlyReserve", preSnap.getMonthlyReserveAmount());
					yesterdayMap.put("debitedAmount", preSnap.getDeductedAmount());
					yesterdayMap.put("totalReserve", preSnap.getTotalReserve());

					toDayMap.put("totalMargin", todaySnap.getTotalMargin());
					toDayMap.put("totalMarginPerc", todaySnap.getTotalMarginPerc());

					toDayMap.put("ytdDisputed", todaySnap.getYtdDisputed());
					toDayMap.put("ytdDisputedPerc", todaySnap.getYtdDisputedPerc());

					toDayMap.put("netMargin", todaySnap.getNetMargin());
					toDayMap.put("netMarginPerc", todaySnap.getNetMarginPerc());

					toDayMap.put("monthlyReserve", todaySnap.getMonthlyReserveAmount());
					toDayMap.put("debitedAmount", todaySnap.getDeductedAmount());
					toDayMap.put("totalReserve", todaySnap.getTotalReserve());
					
					
					projectData.put(dateToFind.getTime(), toDayMap);
					projectData.put(preDateToFind.getTime(), yesterdayMap);
				}
			}
			log.info("---------------------------------"+projectData.toString());
		return projectData;
	}
		public ReserveSnapShot getSnapShot(String businessVertical, Date dateToFind) {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(dateToFind);
			List<ReserveSnapShot> proSnaps = reserveSnapShotRepository.findAllByBuNameAndMonthAndYearAndCreationDate(businessVertical,
					calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR),
					dateToFind);
			
			ReserveSnapShot snap = null;
			if (!proSnaps.isEmpty())
				snap = proSnaps.get(proSnaps.size() - 1);
			return snap;
		}


		
		@Override
		public List<Object> getMarginSnapShot(String accessToken, long to, long from, long projectId,
				String businessVertical) {
			List<Object> dataMargins = new ArrayList<>();
	
			List<String> buisnessVertical = (List<String>) projectInvoiceService.getBusinessVerticals(accessToken);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date(to));
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date dateToFind = calendar.getTime();
			String formattedDate=formatter.format(dateToFind);
			try {
			dateToFind=formatter.parse(formattedDate);
			}
			catch (Exception e) {
				log.error("---------Unable to parse Date----- "+dateToFind);
			}
			
			calendar.setTime(new Date(from));
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			Date preDateToFind = calendar.getTime();
			formattedDate=formatter.format(preDateToFind);
			try {
				preDateToFind=formatter.parse(formattedDate);
			}
			catch (Exception e) {
				log.error("--------Unable to parse Date------ "+preDateToFind);
			}
			List<ProjectSnapshots> snapList = getSnapShot(dateToFind);
			List<ProjectSnapshots> preSnapList = getSnapShot(preDateToFind);
			List<Map<String,Object>>projectsList=(List<Map<String, Object>>) legacyInterface.getBuProjectsForsnapshotsV2(accessToken).get("data");
				for(String bu:buisnessVertical ) {
				Map<Object, Object> buData = new HashMap<>();
				buData.put("buName", bu);
				List<Object> projectsListData = new ArrayList<>();
				List<Map<String, Object>> projects = projectsList.stream().filter(pro->pro.get("businessVertical").toString().equals(bu)).collect(Collectors.toList());
				Double buTodayIC = 0D;
				Double buPreIC = 0D;
				Double buTodayDC = 0D;
				Double buPreDC = 0D;
				Double buTodayRevenue = 0D;
				Double buPreRevenue = 0D;
				Double buCurrentMargin = 0D;
				Double buPriMargin = 0D;
				for (Map<String,Object> projectMap:projects) {
					String projectName = projectMap.get("name").toString();
					Long proId = Long.valueOf(projectMap.get("id").toString());
					Map<Object, Object> projectData = new HashMap<>();
					projectData.put("projectName", projectName);
					projectData.put("projectId", proId);
					Double marginDiff = 0D;
					Map<Object, Object> yesterDayMap = new HashMap<>();
					Map<Object, Object> toDayMap = new HashMap<>();
					
					List<ProjectSnapshots> snapData = snapList.stream().filter(snap->snap.getProjectId().toString().equals(projectMap.get("id").toString())).collect(Collectors.toList());
					ProjectSnapshots snap = null;
					if (!snapData.isEmpty())
						snap = snapData.get(snapData.size() - 1);
					
					List<ProjectSnapshots> preSnapData = preSnapList.stream().filter(preSnap->preSnap.getProjectId().toString().equals(projectMap.get("id").toString())).collect(Collectors.toList());
					ProjectSnapshots preSnap = null;
					if (!preSnapData.isEmpty())
						preSnap = preSnapData.get(preSnapData.size() - 1);

					if(snap!=null) {
						buTodayIC = buTodayIC + snap.getIndirectCost();
						buTodayDC = buTodayDC + snap.getDirectCost();
						buTodayRevenue = buTodayRevenue + snap.getInvoiceAmountInRupees();
					}
					if(preSnap!=null) {
						buPreIC = buPreIC + preSnap.getIndirectCost();
						buPreDC = buPreDC + preSnap.getDirectCost();
						buPreRevenue = buPreRevenue + preSnap.getInvoiceAmountInRupees();
					}
					if (snap != null /* && snap.isChanged() */ && preSnap != null) {
						if (snap.isChanged()) {
							yesterDayMap.put("margin", preSnap.getMargin());
							yesterDayMap.put("projectCost", preSnap.getIndirectCost() + preSnap.getDirectCost());
							yesterDayMap.put("indirectCost", preSnap.getIndirectCost());
							yesterDayMap.put("directCost", preSnap.getDirectCost());
							yesterDayMap.put("revenue", preSnap.getInvoiceAmountInRupees());
							marginDiff = snap.getMargin() - preSnap.getMargin();
							toDayMap.put("margin", snap.getMargin());
							toDayMap.put("projectCost", snap.getIndirectCost() + snap.getDirectCost());
							toDayMap.put("indirectCost", snap.getIndirectCost());
							toDayMap.put("directCost", snap.getDirectCost());
							toDayMap.put("revenue", snap.getInvoiceAmountInRupees());
							projectData.put("marginDiff", Math.round((marginDiff) * 100.00) / 100.00);
							projectData.put("isIndirectCostChanged", false);
							projectData.put("isDcChanged", false);
							projectData.put("isRevenueChanged", false);
							if(preSnap.getIndirectCost()!=snap.getIndirectCost())
								projectData.put("isIndirectCostChanged", true);
							if(preSnap.getInvoiceAmountInRupees()!=snap.getInvoiceAmountInRupees())
								projectData.put("isRevenueChanged", true);
							if(preSnap.getDirectCost()!=snap.getDirectCost())
								projectData.put("isDirectCostChanged", true);
							projectData.put(dateToFind.getTime(), toDayMap);
							projectData.put(preDateToFind.getTime(), yesterDayMap);
							projectsListData.add(projectData);
						}
					}
				}
				if (!projectsListData.isEmpty()) {
					buCurrentMargin = buTodayRevenue - (buTodayDC + buTodayIC);
					if (buPreRevenue != 0)
						buPriMargin = buPreRevenue - (buPreDC + buPreIC);
					else
						buPriMargin = 0D;
					Map<Object, Object> buTodayMargin = new HashMap<>();
					buTodayMargin.put("margin", Math.round((buCurrentMargin) * 100.00) / 100.00);
					buTodayMargin.put("indirectCost", Math.round((buTodayIC) * 100.00) / 100.00);
					buTodayMargin.put("directCost", Math.round((buTodayDC) * 100.00) / 100.00);
					buTodayMargin.put("revenue", Math.round((buTodayRevenue) * 100.00) / 100.00);
					Map<Object, Object> buPreMargin = new HashMap<>();
					buPreMargin.put("margin", Math.round((buPriMargin) * 100.00) / 100.00);
					buPreMargin.put("indirectCost", Math.round((buPreIC) * 100.00) / 100.00);
					buPreMargin.put("directCost", Math.round((buPreDC) * 100.00) / 100.00);
					buPreMargin.put("revenue", Math.round((buPreRevenue) * 100.00) / 100.00);
					if (!buTodayIC .equals(buPreIC))
						buData.put("isIndirectCostChanged", true);
					else
						buData.put("isIndirectCostChanged", false);
					if (!buTodayDC .equals(buPreDC))
						buData.put("isDirectCostChanged", true);
					else
						buData.put("isDirectCostChanged", false);
					if (!buTodayRevenue.equals(buPreRevenue) )
						buData.put("isRevenueChanged", true);
					else
						buData.put("isRevenueChanged", false);
					buData.put(dateToFind.getTime(), buTodayMargin);
					buData.put(preDateToFind.getTime(), buPreMargin);
					buData.put("marginDiff", Math.round((buCurrentMargin - buPriMargin) * 100.00) / 100.00);
					buData.put("projects", projectsListData);
					dataMargins.add(buData);
				}
			}

			return dataMargins;
		}
		
		
		public ProjectSnapshots getSnapShotByProjectId(Long projectId, Date dateToFind) {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(dateToFind);
			List<ProjectSnapshots> proSnaps = snapshotRepository.findAllByProjectIdAndMonthAndYearAndCreationDate(projectId,
					calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR),
					dateToFind);
			
			ProjectSnapshots snap = null;
			if (!proSnaps.isEmpty())
				snap = proSnaps.get(proSnaps.size() - 1);
			return snap;
		}

		public List<ProjectSnapshots> getSnapShot(Date dateToFind) {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(dateToFind);
			List<ProjectSnapshots> proSnaps = snapshotRepository.findAllByMonthAndYearAndCreationDate(
					calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR), dateToFind);
			return proSnaps;
		}

		
		
		/**
		 *@author pankaj
		 *
		 * @apiNote This cron sends mails to respected BU heads if there is a reserve change from current date to previous date
		 * @
		 */
		@SuppressWarnings("unchecked")
		//@Scheduled(cron = "0 0 7 ? * *", zone = "IST") //everyday at 7AM
		@Override
		public void sendMailOnReserveChange(String accessToken) {
			
			Calendar calendar = Calendar.getInstance();
			Date today = new Date();
			Date yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000);
			calendar.setTime(today);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date dateToFind = calendar.getTime();
			String formattedDate = formatter.format(dateToFind);
			try {
				dateToFind = formatter.parse(formattedDate);
			} catch (Exception e) {
				log.error("---------Unable to parse Date----- " + dateToFind);
			}
			calendar.setTime(yesterday);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			Date preDateToFind = calendar.getTime();
			formattedDate = formatter.format(preDateToFind);
			try {
				preDateToFind = formatter.parse(formattedDate);
			} catch (Exception e) {
				log.error("--------Unable to parse Date------ " + preDateToFind);
			}
			List<String> bu = (List<String>) projectInvoiceService.getBusinessVerticals(accessToken);
			for (String businessVertical : bu) {
				Map<String, Object> buHeadInfo = (Map<String, Object>) legacyInterface
						.getBuOwnerInfo(accessToken, businessVertical).get("data");
				if(buHeadInfo != null) {
					String email=dashboardAdminMail;
					String name=buHeadInfo.get("ownerName").toString();
					ReserveSnapShot todaySnap = getSnapShot(businessVertical, dateToFind); // all data
					ReserveSnapShot preSnap = getSnapShot(businessVertical, preDateToFind); // all data
					if (todaySnap!=null && preSnap!=null) {
						if(todaySnap.isChanged()) {
							Context context = new Context();
							context.setVariable("buName", businessVertical);
							context.setVariable("name",name);
							// Today
							context.setVariable("todayDate",today.toString());
							context.setVariable("todayTotalMargin", todaySnap.getTotalMarginPerc().toString() + "%");
							context.setVariable("todayYTD", todaySnap.getYtdDisputedPerc().toString() + "%");
							context.setVariable("todayNetMargin", todaySnap.getNetMarginPerc().toString() + "%");
							context.setVariable("todayBuReserve", "\u20B9" + todaySnap.getMonthlyReserveAmount().toString());
							context.setVariable("todayDebitedAmount", "\u20B9" + todaySnap.getDeductedAmount().toString());
							context.setVariable("todayReserveTotal", "\u20B9" + todaySnap.getTotalReserve().toString());
							// Yesterday
							context.setVariable("yesterdayDate", yesterday);
							context.setVariable("yesterdayTotalMargin", preSnap.getTotalMarginPerc().toString() + "%");
							context.setVariable("yesterdayYTD", preSnap.getYtdDisputedPerc().toString() + "%");
							context.setVariable("yesterdayNetMargin", preSnap.getNetMarginPerc().toString() + "%");
							context.setVariable("yesterdayBuReserve", "\u20B9" + preSnap.getMonthlyReserveAmount().toString());
							context.setVariable("yesterdayDebitedAmount", "\u20B9" + preSnap.getDeductedAmount().toString());
							context.setVariable("yesterdayReserveTotal", "\u20B9" + preSnap.getTotalReserve().toString());
							mailService.sendScheduleHtmlMail(email, "BU Reserve Change", context,"Reserve-Change-Mail.html");
						}
					}	
				}
				
			}
		}		
}