package com.krishna.util;

public class UrlMappings {

	public static final String PREFIX = "/api/v1";
	public static final String CRON_PREFIX = "/api/v1/cron";
	public static final String BU_PREFIX="/api/v1/buDashboard";
	public static final String PROJECT_DASHBOARD_PREFIX="/api/v1/proDashboard";

	

	public static final String ADD_PROJECT_INVOICE = PREFIX + "/accounts/addInvoice";
	public static final String GET_ALL_DATA_ACCOUNTS = PREFIX + "/accounts/getAllData";
	public static final String GET_ALL_BU_DATA_ACCOUNTS = BU_PREFIX + "/accounts/getAllData";

	public static final String ADD_PAYMENT_MODE = PREFIX + "/accounts/addPaymentMode";
	public static final String DELETE_PAYMENT_MODE = PREFIX + "/accounts/deletePaymentMode";
	public static final String GET_ALL_PAYMENT_MODE = PREFIX + "/accounts/getAllPaymentMode";
	public static final String GET_BU_ALL_PAYMENT_MODE = BU_PREFIX + "/accounts/getAllPaymentMode";

	public static final String ADD_INVOICE_CYCLE = PREFIX + "/accounts/addInvoiceCycle";
	public static final String DELETE_INVOICE_CYCLE = PREFIX + "/accounts/deleteInvoiceCycle";
	public static final String GET_ALL_INVOICE_CYCLE = PREFIX + "/accounts/getAllInvoiceCycle";
	public static final String GET_BU_ALL_INVOICE_CYCLE = BU_PREFIX + "/accounts/getAllInvoiceCycle";

	public static final String GET_ALL_INVOICE_STATUS = PREFIX + "/accounts/getAllInvoicetatus";
	public static final String GET_BU_ALL_INVOICE_STATUS = BU_PREFIX + "/accounts/getAllInvoicetatus";

	public static final String EDIT_PROJECT_INVOICE = PREFIX + "/accounts/editProjectInvoice";
	public static final String EDIT_INVOICE_CYCLE = PREFIX + "/accounts/editInvoiceCycle";
	public static final String EDIT_PAYMENT_MODE = PREFIX + "/accounts/editPaymentMode";
	public static final String DELETE_PROJECT_INVOICE = PREFIX + "/accounts/deleteProjectInvoice";
	public static final String ADD_PAYMENT_TERMS = PREFIX + "/accounts/addPaymentTerms";
	public static final String EDIT_PAYMENT_TERMS = PREFIX + "/accounts/editPaymentTerms";
	public static final String DELETE_PAYMENT_TERMS = PREFIX + "/accounts/deletePaymentTerms";
	public static final String GET_ALL_PAYMENT_TERMS = PREFIX + "/accounts/getAllPaymentTerms";
	public static final String GET_BU_ALL_PAYMENT_TERMS = BU_PREFIX + "/accounts/getAllPaymentTerms";

	public static final String GET_ALL_CURRENCIES = PREFIX + "/accounts/getAllCurrencies";
	public static final String GET_BU_ALL_CURRENCIES = BU_PREFIX + "/accounts/getAllCurrencies";

	public static final String GET_ALL_PROJECT_DETAILS = PREFIX + "/accounts/getAllProjectDetails";
	public static final String GET_BU_ALL_PROJECT_DETAILS = BU_PREFIX + "/accounts/getAllProjectDetails";
	public static final String PRO_DASHBOARD_GET_ALL_PROJECT_DETAILS = PROJECT_DASHBOARD_PREFIX + "/accounts/getAllProjectDetails";


	public static final String SET_AMOUNT_IN_RUPEE = PREFIX + "/accounts/setAmountInRupee";
	public static final String GET_PROJECTWISE_DATA = PREFIX + "/accounts/getProjectWiseData";
	public static final String PRO_DASHBOARD_GET_PROJECTWISE_DATA = PROJECT_DASHBOARD_PREFIX + "/accounts/getProjectWiseData";

	public static final String GET_PROJECTWISE_DATA_CLIENT = PREFIX + "/accounts/getProjectWiseDataClient";
	public static final String GET_DATA_FOR_LINE_CHART = PREFIX + "/accounts/getDataForLineChart";
	public static final String SET_PROJECT_IDS = PREFIX + "/invoice/setProjectIds";
	public static final String GET_DATA_CHART = PREFIX + "/invoice/getDataLineChart";
	public static final String GET_INVOICE_TRENDS = PREFIX + "/invoice/getInvoiceTrends";
	public static final String GET_BU_INVOICE_TRENDS = BU_PREFIX + "/invoice/getInvoiceTrends";

	public static final String GET_DISPUTED_INVOICES = PREFIX + "/invoice/getDisputedInvoices";
	public static final String GET_ALL_DISPUTED_INVOICES_OF_PROJECT = PREFIX
			+ "/invoice/getAllDisputedInvoicesOfProject";
	public static final String GET_DISPUTED_PERCENTAGE = PREFIX + "/invoice/getDisputedPercentage";
	public static final String GET_AVERAGE_DISPUTED_PERCENTAGE = PREFIX + "/invoice/getAverageDisputedPercentage";
	public static final String GET_LTM_DISPUTED_PERCENTAGE = PREFIX + "/invoice/getLTMDisputedPercentage";
	public static final String GET_DISPUTED_INVOICES_OF_PROJECT = PREFIX + "/invoice/getDisputedInvoicesOfProject";
	public static final String INVOICE_CREATION_REMINDER = PREFIX + "/invoice/creationReminder";
	public static final String MESSAGE_FILE_PATH = "messages/message.properties";
	public static final String USER_VERIFY_PATH = PREFIX + "/login/filter";
	public static final String ADD_PROJECT_MARGIN = PREFIX + "/accounts/addProjectMargin";
	public static final String EDIT_PROJECT_MARGIN = PREFIX + "/accounts/editProjectMargin";
	public static final String DELETE_PROJECT_MARGIN = PREFIX + "/accounts/deleteProjectMargin";
	public static final String GET_ALL_PROJECT_MARGIN_DATA = PREFIX + "/accounts/getAllProjectMarginData";
	public static final String GET_RECEIVABLE = PREFIX + "/accounts/getReceivable";
	public static final String CHANGE_INVOICE_STATUS = CRON_PREFIX + "/accounts/changeInvoiceStatus";
	public static final String GET_BUSINESS_VERTICAL = PREFIX + "/accounts/getBusinessVertical";
	public static final String GET_PROJECT_WISE_INVOICE_STATUS = PREFIX +"/accounts/getProjectWiseInvoiceStatus";
	public static final String GET_PROJECT_WISE_INVOICES = PREFIX +"/accounts/getProjectWiseInvoices";
	public static final String COMPLIANCE_CRON = CRON_PREFIX + "/accounts/complianceCron";
	public static final String GET_PREVIOUS_MONTH_INVOICES = PREFIX + "/accounts/getPreviousMonthInvoices";
	public static final String GET_BU_PREVIOUS_MONTH_INVOICES = BU_PREFIX + "/accounts/getPreviousMonthInvoices";
	public static final String PRO_DASHBOARD_GET_PREVIOUS_MONTH_INVOICES = PROJECT_DASHBOARD_PREFIX + "/accounts/getPreviousMonthInvoices";


	public static final String MARK_INVOICE_DISPUTED = CRON_PREFIX + "/accounts/invoice/disputed";
	public static final String SAVE_DISPUTED_COMMENT_FOR_LEGAL = PREFIX + "/invoice/disputedComment/save";
	
	
	/** Invoice */
	public static final String INVOICE_BANKS = PREFIX + "/invoice/bank";
	public static final String BU_INVOICE_BANKS = BU_PREFIX + "/invoice/bank";

	public static final String INVOICE_SOURCES = PREFIX + "/invoice/source";
	public static final String BU_INVOICE_SOURCES = BU_PREFIX + "/invoice/source";

	public static final String INVOICE_CREATION = PREFIX + "/accounts/invoice/add";
	public static final String INVOICE_HISTORY = PREFIX + "/invoice/history";
	public static final String GET_INVOICE_TYPE = PREFIX + "/accounts/invoiceType";
	public static final String PROJECT_SETTINGS = PREFIX + "/accounts/invoice/projectSettings/projectId";
	public static final String VIEW_INVOICE_DETAILS = PREFIX + "/accounts/invoice/view";
	public static final String INVOICE_ITEM = PREFIX + "/accounts/invoice/item";
	public static final String SAVE_INVOICE_SLIP = PREFIX + "/accounts/invoiceSlip/save";
	public static final String DOWNLOAD_INVOICE_SLIP = PREFIX + "/accounts/invoiceSlip/download";
	public static final String RESET_INVOICE = PREFIX +"/accounts/invoiceSlip/reset";
	public static final String GET_BUSINESS_AMOUNT = PREFIX + "/accounts/overallBusinessAmount";
	public static final String GET_BU_BUSINESS_AMOUNT = BU_PREFIX + "/accounts/overallBusinessAmount";

	public static final String SEND_THANK_YOU_MAIL = PREFIX + "/accounts/sendThankYouMail";
	public static final String GET_CLIENT_EMAILS = PREFIX + "/accounts/getClientEmails";
	
	/** Account Compliance */
	public static final String UPDATE_COMPLIANCE_STATUS = PREFIX + "/project/accountsCompliance/issueType";
	
	/** Indirect Cost */

	public static final String ADD_INDIRECT_COST = PREFIX + "/accounts/IndirectCost/addIndirectCost";
	public static final String GET_INDIRECT_COSTS = PREFIX + "/accounts/IndirectCost/getIndirectCost";
	public static final String UPDATE_INDIRECT_COSTS = PREFIX + "/accounts/IndirectCost/updateIndirectCost";
	public static final String DELETE_INDIRECT_COST = PREFIX + "/accounts/IndirectCost/deleteIndirectCost";
	public static final String GRAPH_DATA = PREFIX + "/accounts/IndirectCost/graph";
	public static final String GET_STAFF_COST_PROJECTS = PREFIX + "/getStaffCostProjects";
	public static final String GET_VARIABLE_COST = PREFIX + "/getVariableCost";
	public static final String GET_INFRA_COST = PREFIX + "/getInfraCost";
	public static final String GET_REIMBURSEMENT = PREFIX + "/getReimbursement";
	public static final String GET_VERTICAL_COST = PREFIX + "/accounts/indirectCost/getVerticalCost";
	public static final String GET_BU_INDIRECT_TOTAL = PREFIX + "/accounts/indirectCost/getBuIndirectCost";
	public static final String GET_TOTAL_EMPLOYEE_BREAKUP = PREFIX + "/accounts/indirectCost/getTotal";
	public static final String GET_BU_ALL_PROJECTS = PREFIX + "/getBuAllProjects";
	public static final String GET_BU_BILLABLE_PROJECTS = PREFIX + "/getBuBillableProjects";
	public static final String GET_PER_PERSON_HOURS = PREFIX + "/accounts/getPerPersonHours";
	public static final String GET_GRADE_BASED_INDIRECT_COST = PREFIX + "/accounts/indirectCost/gradeBased";
	public static final String SAVE_FIXED_COST = PREFIX + "/indirectCost/grade/fixedCost";
	public static final String GET_ALL_INDIRECTCOST_GRADE_BASED = PREFIX + "/indirectCost/gradeBased";
	public static final String SET_MARGIN_BASIS = PREFIX + "/indirectCost/setMarginBasis";
	public static final String GET_MARGIN_BASIS = PREFIX + "/accounts/marginbasis";
	public static final String BU_GET_MARGIN_BASIS = BU_PREFIX + "/accounts/marginbasis";

	public static final String CARRY_FORWARD_GRADE_IC = PREFIX + "/indirectCost/gradeBased/carryforward";
	public static final String GET_ASSET_COST = PREFIX + "/getAssetCost";
	public static final String PREVIOUS_MONTH_GIC = CRON_PREFIX + "/indirectCost/previousMonthGic";

	/** Pay Register Url Constants */

	public static final String CREATE_PAYREGISTER = PREFIX + "/payRegister/createPayroll";
	public static final String GET_PAYREGISTERS = PREFIX + "/payRegister/getAllPayRegisters";
	public static final String GET_BANKS = PREFIX + "/payRegister/getBanks";
	public static final String GET_PAYREVISIONS = PREFIX + "/payRegister/getPayRevision/{userId}";
	public static final String GET_USER_ACCOUNTDETAILS = PREFIX + "/payRegister/getUserAccountDetails";
	public static final String EDIT_PAYREVISION = PREFIX + "/payRegister/editPayrevision";
	public static final String DELETE_PAYREVISION = PREFIX + "/payRevision/deletePayrevision";
	public static final String GET_SUM_FOR_WIDGETS = PREFIX + "/payRegister/getTotalSum";
	public static final String GET_REVISION_GAP = PREFIX + "/payRegister/getRevisionGap";
	public static final String GET_PAYREGISTER_USERS = PREFIX + "/payRegister/getPayregisterUsers";

	/** Pay Roll Url Constants */

	public static final String GENERATE_PAYROLL = PREFIX + "/payroll/generatePayroll";
	public static final String EDIT_PAYROLL = PREFIX + "/payroll/editPayroll";
	public static final String GET_PAYROLLS = PREFIX + "/payroll/getPayrolls";
	public static final String GET_PAYROLLS_TIMESHEET = PREFIX + "/payroll/getPayrollsTimesheet";
	public static final String GET_MONTHWISE_PAYROLLS = PREFIX + "/payroll/getMonthwisePayrolls";
	public static final String GET_EMPLOYEE_ARREARS = PREFIX + "/payroll/getEmployeeArrears/{payrollId}";
	public static final String GET_USER_DETAILS = PREFIX + "/payroll/getUserDetails";
	public static final String GET_PAYSLIP_DATA = PREFIX + "/payroll/getPaySlipData";
	public static final String GET_PAYROLL_USERS = PREFIX + "/payroll/getAllPayrollUsers";
	public static final String VERIFY_ATTENDANCE = PREFIX + "/payroll/verifyAttendance";
	public static final String VERIFY_TIMESHEET = PREFIX + "/payroll/verifyTimesheet";
	public static final String GET_TIMESHEET_MAIL_HISTORY = PREFIX + "payroll/getTimeSheetMailHistory";
	public static final String DOWNLOAD_ARREAR_FILE = PREFIX + "/payroll/downloadArrearFile";
	public static final String UNVERIFY_ATTENDANCE = PREFIX + "/payroll/unverifyAttendance";
	public static final String GET_NET_PAY_FOR_EXPORT = PREFIX + "/payroll/getNetPayForExport";
	public static final String GET_USER_LEAVES_DATA = PREFIX + "/payroll/getUserLeavesData";
	public static final String SEND_MAIL_ON_NONCOMPLIANT_TIMESHEET = PREFIX
			+ "/payroll/sendMailOnNonCompliantTimesheet";
	public static final String SAVE_PAYROLL_COMMENT = PREFIX + "/payroll/savePayrollComment";
	public static final String GET_PAYROLL_COMMENTS = PREFIX + "/payroll/getPayrollComments";
	public static final String GET_PAYROLL_WIDGETS_DATA = PREFIX + "/payroll/getPayRollWidgetsData";
	public static final String DELETE_PAYROLL = PREFIX + "/payroll/delete/{payrollId}";
	public static final String GENERTE_PAYROLL_BY_CRON = CRON_PREFIX + "/payroll/generatePayrollByCron";
	public static final String CHANGE_PAYROLL_AND_INVOICE_STATUS = PREFIX + "/payroll/revertPayroll";
	public static final String SET_PAYROLL_ON_PRIORITY = PREFIX + "/payroll/OnPriority";
	public static final String SET_PAID_DAYS = PREFIX + "/payroll/setPaidDays";
	public static final String RESET_PAYROLL_PRIORITY = PREFIX + "/payroll/resetPriority";
	public static final String ADD_PAYROLL_COMMENTS = PREFIX + "/payroll/comments";
	public static final String GET_PAYROLL_DETAILS = PREFIX + "/payroll/userDetails";
	public static final String EXCLUDE_FOR_MARGIN = PREFIX + "/payroll/excludeForMargin";
	public static final String DELETE_ARREAR = PREFIX + "/arrear/delete/{id}";
	public static final String GET_BU_WISE_REIMBURSEMENT = PREFIX + "/payroll/getBuWiseReimbursment";
	public static final String SET_BU_HEAD_PAYROLL_APPROVAL = PREFIX + "/payroll/setBuHeadPayRollApproval";
	public static final String FLUSH_PAYROLL_CACHE = PREFIX + "/payroll/flushPayrollCache";
	public static final String GET_BU_APPROVAL_COMMENT = PREFIX + "/payroll/getBuApprovalComment";
	public static final String GET_PAYROLL_STATUS_LIST =PREFIX+ "/payroll/getPayrollStatusList";
	public static final String SEND_BU_HEAD_APPROVAL_MAIL= PREFIX+ "/payroll/sendBuHeadApprovalEmail"; 
	public static final String FLUSH_PAYROLL_USER_CACHE = PREFIX + "/payroll/flushPayrollUserCache";

	/** Pay Roll Url Constants For buDashboard */
	public static final String GET_BU_DASHBOARD_PAYROLLS = PREFIX + "/buDashboard/payroll/getBuWisePayrolls";
	public static final String GET_BU_DASHBOARD_MONTHWISE_PAYROLLS = PREFIX + "/buDashboard/payroll/getBuWiseMonthwisePayrolls";
	public static final String GET_BU_DASHBOARD_USER_LEAVES_DATA = PREFIX + "/buDashboard/payroll/getUserLeavesData";
	
	public static final String GET_BU_DASHBOARD_PAYREGISTER_USERS = PREFIX + "/buDashboard/payRegister/getBuWisePayregisterUsers";
	public static final String GET_BU_DASHBOARD_PAYREGISTERS = PREFIX + "/buDashboard/payRegister/getAllBuWisePayRegisters";
	
	/** Arrears/Reimbursement */
	public static final String SAVE_ARREAR = PREFIX + "/payroll/saveArrear/{userId}";
	public static final String GET_ALL_ARREARS = PREFIX + "/payroll/getAllArrears";
	public static final String EDIT_ARREARS = PREFIX + "/payroll/editArrear/{arrearId}";
	public static final String GET_ARREARS_WIDGET = PREFIX + "/payroll/getArrearWidgetData";
	
	/** Incentive */
	public static final String INCENTIVES = PREFIX + "/payroll/previousArrears";
	public static final String GET_ALL_INCENTIVES = PREFIX + "/payroll/previousArrears/all";
	public static final String EDIT_INCENTIVES = PREFIX + "/payroll/previousArrears/{arrearId}";
	public static final String GET_INCENTIVE_WIDGET = PREFIX + "/payroll/previousArrears/widget";

	/** Email Template */

	public static final String CREATE_EMAIL_TEMPLATE = PREFIX + "/email/template";
	public static final String SEND_EMAIL_TEMPLATE = PREFIX + "/email/template/send";

	/** Pay Slip Url Constants */

	public static final String GET_PAYSLIP = PREFIX + "/payslip/getPayslip";
	public static final String CREATE_PAYSLIP = PREFIX + "/payslip/createPayslip";
	public static final String SAVE_PAYSLIP = PREFIX + "/payslip/savePayslip";
	public static final String GENERATE_PAYSLIPS = PREFIX + "/payslip/generatePayslips";
	public static final String SEND_PAYSLIP = PREFIX + "/payslip/sendPayslip";
	public static final String GETACCOUNTDETAILS = PREFIX + "/payslip/getAccountDetailsForMyAccount";
	public static final String CHANGE_PAYROLL_STATUS_ON_EXPORT = PREFIX + "/payslip/changePayrollStatusOnExport";
	public static final String EXPORT_PAYROLL_TEXT_FILE = PREFIX + "/payslip/exportTextFile";

	/** widget */
	public static final String INVOICE_DATA_WIDGET = PREFIX + "/widget/invoice";

	/** Interest free security deposit */

	public static final String ADD_SECURITY_DEPOSIT = PREFIX + "/accounts/addSecurityDeposit";
	public static final String EDIT_SECURITY_DEPOSIT = PREFIX + "/accounts/editSecurityDeposit";
	public static final String DELETE_SECURITY_DEPOSIT = PREFIX + "/accounts/deleteSecurityDeposit";
	public static final String GET_ALL_SECURITY_DEPOSITS = PREFIX + "/accounts/getAllSecurityDeposits";
	public static final String GET_BU_ALL_SECURITY_DEPOSITS = BU_PREFIX + "/accounts/getAllSecurityDeposits";

	public static final String GET_PROJECTWISE_SECURITY_DATA = PREFIX + "/accounts/getAllProjectWiseSecurityDeposits";
	public static final String PRO_DASHBOARD_GET_PROJECTWISE_SECURITY_DATA = PROJECT_DASHBOARD_PREFIX + "/accounts/getAllProjectWiseSecurityDeposits";

	public static final String GET_PROJECTWISE_SECURITY_DATA_CLIENT = PREFIX
			+ "/accounts/getAllProjectWiseSecurityDepositsClient";
	public static final String GET_SECURITY_DEPOSITE_STATUS = PREFIX
			+ "/accounts/getSecurityDepositeStatus";
	public static final String GET_BU_SECURITY_DEPOSITE_STATUS = BU_PREFIX
			+ "/accounts/getSecurityDepositeStatus";
	public static final String GET_IFSD_ADJUSTED_INVOICES = PREFIX + "/securityDeposit/get/invoice";

	/** Mail Notification */
	public static final String GET_MAIL_NOTIFICATION_DATA = PREFIX + "/mailNotification/getMailNotificationsData";

	/** Dashboard Admin */
	public static final String SET_ANNUAL_CTC = PREFIX + "/dashboardAdmin/setAnnualCtc";
	public static final String SET_EFFECTIVE_DATE = PREFIX + "/dashboardAdmin/setEffectiveDate";
	public static final String FLUSH_CACHES = PREFIX + "/dashboardAdmin/flushCache";
	public static final String REPUTCOMPANYCACHE = CRON_PREFIX + "/dashboardAdmin/companyCache";
	public static final String REPUTPAYCACHE = PREFIX + "dashboardAdmin/payCache";
	public static final String FLUSH_TIMESHEET_CACHE = CRON_PREFIX + "/dashboardAdmin/flushTimesheetCache";
	public static final String SAVE_PROJECT_SNAPSHOT = PREFIX + "/dashboardAdmin/saveProjectSnapshot";
	public static final String SAVE_PROJECT_SNAPSHOT_MONTH = PREFIX + "/dashboardAdmin/saveProjectSnapshotMonthwise";

	/** Margin */
	public static final String GET_DIRECT_COST = PREFIX + "/indirectCost/getDirectCost";
	public static final String PRO_DASHBOARD_GET_DIRECT_COST = PROJECT_DASHBOARD_PREFIX + "/indirectCost/getDirectCost";

	public static final String GET_INVOICE_FOR_BU_MARGINS = PREFIX + "/buMargin/getInvoiceForBuMargins";
	public static final String GET_BU_MARGIN = PREFIX + "/indirectCost/getBuMargin";
	public static final String GET_BU_INDIRECT_COST = PREFIX + "/buMargin/getBuIndirectCost";
	public static final String GET_BU_MARGIN_VERTICAL_COST = PREFIX + "/buMargin/buMarginVerticalCost";
	public static final String GET_INVOICE_TOTAL_COMPANY = PREFIX + "/companyMargin/getInvoiceTotalBuWise";
	public static final String GET_BU_TOTAL_MARGIN = PREFIX + "/buMargin/getBuTotalMargin";
	public static final String GET_PROJECT_COST_TOTAL_COMPANY = PREFIX + "/buMargin/getDirectCostBuWise";
	public static final String GET_COMPANYWISE_DATA = PREFIX + "/companyMargin/getCompanywiseData";
	public static final String GET_COMPANY_MARGIN = PREFIX + "/companyMargin/getCompanyMargin";
	public static final String GET_COMPANY_PL = PREFIX + "/companyMargin/getCompanyPL";
	public static final String GET_TOTAL_COST_DIVISION = PREFIX + "/companyMargin/getTotaCostDivision";
	public static final String GET_INDIRECT_COST_DIVISION = PREFIX + "/companyMargin/getIndirectCostDivision";
	public static final String GET_LIFETIME_INVOICE = PREFIX + "/margin/getLifetimeInvoices";
	public static final String GET_LIFETIME_RESOURCES = PREFIX + "/margin/getLifetimeResources";
	public static final String GET_LIFETIME_LEAVES = PREFIX + "/margin/getLifeTimeLeaves";
	public static final String GET_LIFETIME_EXPECTED_HOURS = PREFIX + "/margin/getLifeTimeExpectedHours";
	public static final String GET_LIFETIME_MARGIN = PREFIX + "/margin/getLifeTimeMargin";
	public static final String GET_LIFETIME_INDIRECTCOST = PREFIX + "/margin/getLifeTimeIndirectCost";
	public static final String GET_MARGIN_SNAPSHOT = PREFIX + "/margin/getmarginsnapShot";
	public static final String GET_USER_SNAPSHOT = PREFIX + "/margin/getUserSnapShot";
	public static final String GET_GRADEWISE_COST_BU_MARGIN = PREFIX + "/buMargin/gradeBasedCost";
	public static final String GET_LIFETIME_GRADEWISECOST = PREFIX + "/margin/lifeTimeGradewise";
	public static final String GET_UIC_PER_SEAT = PREFIX + "/buMargin/uicPerSeat";
	public static final String GET_REIMBURSEMENT_COST = PREFIX + "/companyMargin/getReimbursementCost";
	public static final String FLUSH_DIRECT_COST_CACHE = CRON_PREFIX + "/companyMargin/flushDirectCostCache";
	public static final String FLUSH_TEAM_DATA = CRON_PREFIX + "/companyMargin/flushTeamData";
	public static final String FLUSH_PROJECT_DETAILS_DATA = CRON_PREFIX + "/projectInvoice/flushProjectDetailsCache";
	
	public static final String FLUSH_TOTAL_BU_MARGIN = CRON_PREFIX + "/buMargin/flushTotalBuMargin";
	public static final String FLUSH_BU_MARGINS = CRON_PREFIX + "/buMargin/flushBuMargins";
	public static final String FLUSH_LIFETIME_INDIRECT_COST = CRON_PREFIX + "/buMargin/flushLifeTimeIndirectCost";
	public static final String SAVE_PROJECT_SNAPSHOT_AUTO = CRON_PREFIX + "/buMargin/saveProjectSapshotAuto";
	public static final String SEND_MAIL_ON_RESERVE_CHANGE = CRON_PREFIX + "/sendMailOnReserveChange";
	public static final String GET_YEARLY_FORECAST_REVENUE =  "/yearly/forecastedRevenue";
	public static final String GET_BU_YEARLY_FORECAST_REVENUE =  BU_PREFIX + "/yearly/forecastedRevenue";
	public static final String GET_ACTUAL_HRS_INVOICE_PIPELINE =  BU_PREFIX + "/invoicePipeline/getActualHoursForInvoicePipeline";

 
	
	/** BU Reserve*/
	public static final String GET_BU_RESERVES = PREFIX + "/accounts/buReserve";

	/** Leave Cost Controller. */
	public static final String GET_ALL_LEAVECOST_PERCENT = PREFIX + "/leaveCostPercent/getAll";
	public static final String ADD_LEAVECOST_PERCENT = PREFIX + "/leaveCostPercent/save";
	public static final String UPDATE_LEAVECOST_PERCENT = PREFIX + "/leaveCostPercent/update";
	public static final String DELETE_LEAVECOST_PERCENT = PREFIX + "/leaveCostPercent/delete";
	public static final String GET_CURRENT_LEAVE_COST_PERCENT = PREFIX + "/leaveCostPercent/currentLeaveCost";

	/** Dollar Cost */
	public static final String GET_ALL_DOLLAR_COST = PREFIX + "/dollarCost/getAll";
	public static final String ADD_DOLLAR_COST = PREFIX + "/dollarCost/save";
	public static final String UPDATE_DOLLAR_COST = PREFIX + "/dollarCost/update";
	public static final String DELETE_DOLLAR_COST = PREFIX + "/dollarCost/delete";
	public static final String GET_DOLLAR_COST_BY_MONTH_YEAR = PREFIX + "/dollarCost/getByMonthAndYear";
	public static final String DOLLAR_COST_LAST_6_MONTH = PREFIX + "/dollarCost/average";
	public static final String DOLLAR_COST_MONTH = PREFIX + "/dollarCost/getDollarCostOfMonth";
	public static final String DOLLAR_COST_LAST_12_MONTH = PREFIX + "/dollarCost/ltmAverage";


	/** Utility */
	public static final String GET_LAPTOP_ALLOWANCE = PREFIX + "/util/getLaptopAllowanceForAsset";
	public static final String GET_ENCRYPTED_MONTHLY_PAY = PREFIX + "/indirectCost/getEarnedMonthlyPay";
	public static final String TOGGLE_SERVER = PREFIX + "/util/toggleServer";
	public static final String USE_OLAP_SERVER = PREFIX + "/util/useAnalyticServer";

	/** Project Invoice Utility */
	public static final String GET_ACTUAL_INVOICE = PREFIX + "/projectInvoiceUtil/getActualInvoiceValue";
	public static final String GET_BU_ACTUAL_INVOICE = BU_PREFIX + "/projectInvoiceUtil/getActualInvoiceValue";


	public static final String CHECK_IS_SERVER_RUNNING = "/accounts/isRunning";

	public static final String IS_CONNECTED = "/accounts/isConnected";

	public static final String IS_ATTENDANCE_VERIFIED = "/payroll/isAttedanceVerified";

	/**
	 * Payroll Trends Constants
	 */
	public static final String GET_PAYROL_TRENDS = PREFIX + "/payrollTrends/";
	public static final String GET_MIN_MAX_SALARY_OF_GIVEN_GRADE = PREFIX + "/getMinAndMaxSalaryOfGrade";
	public static final String SAVE_EXPECTED_BILLING_RATE = PREFIX + "/saveExpectedBillingRate";
	public static final String CARRY_FORWARD_BILLING_RATE = PREFIX + "/carryForwardBillingRate";
	public static final String CARRY_FORWARD_BILLING_RATE_CRON = CRON_PREFIX + "/carryForwardBillingRateCron";


	/** Payroll Payable Url Constants */
	public static final String GET_ALL_ACCOUNTS_PAYABLES = PREFIX + "/accounts/payable";
	public static final String SAVE_ACCOUNT_PAYABLE = PREFIX + "/accounts/payable";
	public static final String UPDATE_ACCOUNT_PAYABLE = PREFIX + "/accounts/payable";
	public static final String DELETE_ACCOUNT_PAYABLE = PREFIX + "/accounts/payable";
	public static final String GET_ACCOUNT_PAYABLE_TYPES = PREFIX + "/accounts/payable/getTypes";
	public static final String GET_ALL_TAX_TYPES = PREFIX + "/accounts/payable/getTaxTypes";
	public static final String CALCULATE_TAXES = PREFIX + "/accounts/payable/calculateTaxes";
	public static final String GET_ALL_PAYABLE_STATUS = PREFIX + "/accounts/payable/getAllStatus";

	/** HsnCode Url Constants */
	public static final String GET_ALL_HSN_CODES = PREFIX + "/accounts/hsnCode";
	public static final String SAVE_HSN_CODE = PREFIX + "/accounts/hsnCode";
	public static final String UPDATE_HSN_CODE = PREFIX + "/accounts/hsnCode";
	public static final String DELETE_HSN_CODE = PREFIX + "/accounts/hsnCode";

	/** Account Head Url Constants */
	public static final String GET_ALL_ACCOUNTS_HEAD = PREFIX + "/accounts/accounthead";
	public static final String SAVE_ACCOUNTS_HEAD = PREFIX + "/accounts/accounthead";
	public static final String UPDATE_ACCOUNTS_HEAD = PREFIX + "/accounts/accounthead";
	public static final String DELETE_ACCOUNTS_HEAD = PREFIX + "/accounts/accounthead";

	/** Invoice Pipeline */
	public static final String GET_INVOICE_PIPELINE = PREFIX + "/invoicePipeline";
	public static final String GET_BU_INVOICE_PIPELINE = BU_PREFIX + "/invoicePipeline";

	public static final String FLUSH_ACTUAL_HOURS = PREFIX + "/invoicePipeline/flushActualHours";


	/** Consolidated */
	public static final String GET_SALARY_RECONCILIATION = PREFIX + "/util/getConsolidatedTimesheetUser";
	public static final String GET_USERS_SALARY_RECONCILIATION = PREFIX + "/util/getUsersReconciliation";
	public static final String GET_SALARY_RECONCILIATION_IC = PREFIX + "/util/getIndirectCostForSalaryReconciliation";
	public static final String GET_SALARY_DIFFERENCE = PREFIX + "/util/getSalaryDifference";
	public static final String GET_AVERAGE_BILLING = PREFIX + "/util/getAverageBilling";
	public static final String GET_BU_AVERAGE_BILLING = PREFIX + "/buDashboard/getAverageBilling";
	public static final String GET_BU_DASHBOARD_AVERAGE_BILLING = BU_PREFIX + "/buDashboard/getAverageBilling";
	public static final String GET_LIFETIME_AVERAGE_BILLING = PREFIX + "/util/getLifetimeAverageBilling";


	public static final String SEND_BILLING_COMPLIANCE_MAIL = PREFIX + "/util/sendProjectBillingComplianceMail";
	public static final String SEND_BU_BILLING_COMPLIANCE_MAIL = PREFIX + "/util/sendBillingComplianceMail";
	public static final String FLUSH_USER_CACHE = PREFIX + "/util/flushUserCache";
	public static final String DIRECT_COST_FORECAST = PREFIX + "/costForecast";
	public static final String INDIRECT_COST_FORECAST = PREFIX + "/indirectCostForecast";
	
	public static final String GIC_YEARLY = PREFIX + "/indirectCost/gradeBased/yearly";

	public static final String YEARLY_DIPUTED_INVOICES =PREFIX + "/projectInvoice/disputed/yearly";
	public static final String BU_YEARLY_DIPUTED_INVOICES =BU_PREFIX + "/projectInvoice/disputed/yearly";
	public static final String BU_DISPUTED_INVOICE_LIST =BU_PREFIX + "/projectInvoice/getDisputedInvoiceListForBu";



	public static final String PENDING_INVOICES_OF_PROJECT = PREFIX + "/invoices/status/pending";
	public static final String CHANGE_INVOICE_STATUS_DISPUTED = PREFIX + "/invoices/status/disputed";
	public static final String SAVE_AVERAGE_BILLING_COMPLIANCE_COMMENTS = PREFIX + "/averagebillingcompliance/comments";
	public static final String DELETE_COMPLIANCE_COMMENTS = PREFIX + "/averagebillingcompliance/delete/comments";
	public static final String GET_PAYMENT_MODE_PIECHART = PREFIX + "/projectInvoice/paymentMode/pieChart";
	
	public static final String GET_PROJECT_BILLING = PREFIX + "/averageBilling/project";

	public static final String GET_PROJECT_BILLING_YEARLY = PREFIX + "/averageBilling/projectYearly";
	public static final String PRO_DASHBOARD_GET_PROJECT_BILLING_YEARLY = PROJECT_DASHBOARD_PREFIX + "/averageBilling/projectYearly";


	
	public static final String SEND_MAIL_ON_ACCOUNTS_COMPLIANT_STATUS_CHANGE = PREFIX + "/projectAccountCompliance/sendMailOnAccountsCompliantStatusChange";


	public static final String GET_PENDING_INVOICES_DATA = PREFIX + "/accounts/getPendingInvoiceData";
	
	public static final String GET_COUNTRY_WISE_PIE_CHART = PREFIX + "/projectInvoice/getCountryWisePieChart";

	
	public static final String BU_RESERVE_UPDATE_DEDUCTED_AMOUNT = PREFIX + "/buReserve/updateDeductedAmount";

	public static final String GET_BU_WISE_RESERVE = PREFIX + "/buReserve/getBuWiseReserve";
	public static final String BU_GET_BU_WISE_RESERVE = BU_PREFIX + "/buReserve/getBuWiseReserve";

	
	public static final String DELETE_BU_WISE_RESERVE = PREFIX + "/buReserve/deleteBuReserve";
	
	public static final String UPDATE_BU_WISE_REMARKS = PREFIX + "/buReserve/updateRemarks";
	
	public static final String DEDUCTION_CRON = CRON_PREFIX + "/buReserve/sendMailOnDeductedAmount";
	public static final String GET_PROJECT_WISE_TOTAL_REVENUE = PREFIX + "/projectInvoice/getProjectWiseTotalRevenue";
	public static final String GET_BU_WISE_TOTAL_REVENUE = PREFIX + "/projectInvoice/getBuWiseTotalRevenue";
	public static final String GET_BU_TOTAL_REVENUE = BU_PREFIX + "/projectInvoice/getBuWiseTotalRevenue";

	public static final String GET_MANAGER_WISE_QUARTERLY_REVENUE = PREFIX + "/projectInvoice/getManagerWiseQuarterlyRevenue";
	public static final String GET_CLIENT_WISE_COUNTRY_LIST = PREFIX + "/projectInvoice/getClientWiseCountryList";
	public static final String PRO_DASHBOARD_GET_CLIENT_WISE_COUNTRY_LIST = PROJECT_DASHBOARD_PREFIX + "/projectInvoice/getClientWiseCountryList";

	public static final String RESERVE_PERCENTAGE_CRON = CRON_PREFIX + "/buReserve/reservePercentageCron";
	public static final String DATA_CORRECT = PREFIX + "/buReserve/dataCorrect";

	//SnapShot Controller
	public static final String GET_BU_WISE_MARGIN_SNAPSHOT = PREFIX + "/snapShot/getBuWiseMarginSnapShot";
	public static final String GET_BU_WISE_RESERVE_SNAPSHOT = PREFIX + "/snapShot/getBuWiseReserveSnapShot";

	//DeliveryTeam Controller
	public static final String GET_PROJECT_MARGIN = PREFIX + "/deliveryTeam/getProjectMargin";
	public static final String BU_GET_PROJECT_MARGIN = BU_PREFIX + "/deliveryTeam/getProjectMargin";

	public static final String GET_TEAM_HEAD_WISE_DATA_LINE_CHART = PREFIX + "/deliveryTeam/getTeamHeadWiseDataLineChart";
	public static final String BU_GET_TEAM_HEAD_WISE_DATA_LINE_CHART = BU_PREFIX + "/deliveryTeam/getTeamHeadWiseDataLineChart";

	public static final String GET_TEAM_HEAD_WISE_AVERAGE_DISPUTED_INVOICE_PERCENTAGE = PREFIX + "/deliveryTeam/getTeamHeadWiseAverageDisputedInvoicePercentage";
	public static final String BU_GET_TEAM_HEAD_WISE_AVERAGE_DISPUTED_INVOICE_PERCENTAGE = BU_PREFIX + "/deliveryTeam/getTeamHeadWiseAverageDisputedInvoicePercentage";

	public static final String GET_TEAM_HEAD_WISE_INVOICE_DATA = PREFIX + "/deliveryTeam/getTeamHeadWiseInvoiceData";
	public static final String GET_TEAM_HEAD_FILTER_WISE_INVOICE_DATA = PREFIX + "/deliveryTeam/getTeamHeadFilterWiseInvoiceData";
	public static final String BU_GET_TEAM_HEAD_FILTER_WISE_INVOICE_DATA = BU_PREFIX + "/deliveryTeam/getTeamHeadFilterWiseInvoiceData";

	public static final String GET_TEAM_HEAD_WISE_IFSD_DATA = PREFIX + "/deliveryTeam/getTeamHeadWiseIfsdData";
	public static final String BU_GET_TEAM_HEAD_WISE_IFSD_DATA = BU_PREFIX + "/deliveryTeam/getTeamHeadWiseIfsdData";

	public static final String GET_TEAM_HEAD_WISE_INVOICE_BILLING = PREFIX + "/deliveryTeam/getInvoiceTrends";
	public static final String GET_OVERDUE_INVOICE = PREFIX + "/deliveryTeam/disputed/yearly";
	public static final String GET_TEAM_HEAD_WISE_INVOICE_PIPELINE = PREFIX + "/deliveryTeam/invoicePipeline";
	public static final String GET_TEAM_HEAD_WISE_TOTAL_MARGIN = PREFIX + "/deliveryTeam/getTotalMargin";
	public static final String BU_GET_TEAM_HEAD_WISE_TOTAL_MARGIN = BU_PREFIX + "/deliveryTeam/getTotalMargin";

	public static final String GET_CUMMULATIVE_DATA = PREFIX + "/deliveryTeam/getCumulativeData";
	public static final String BU_GET_CUMMULATIVE_DATA = BU_PREFIX + "/deliveryTeam/getCumulativeData";

	public static final String GET_TEAM_HEAD_WISE_YTD = PREFIX + "/deliveryTeam/getDeliveryHeadWiseYTD";
	
	public static final String GET_SCHEDULED_CRONE = PREFIX + "/job/getBuReserveCroneSix";
	public static final String GET_YTD_BIFURCATION = PREFIX + "/deliveryTeam/getYTDBifurcation";
	public static final String GET_LTM_BIFURCATION = PREFIX + "/deliveryTeam/getLTMBifurcation";



	//VariablePay
	public static final String ADD_VARIABLE_PAY = PREFIX + "/variablePay/addVariablePay";;
	public static final String DELETE_VARIABLE_PAY = PREFIX + "/variablePay/deleteVariablePay";
	public static final String GET_VARIBALE_PAY = PREFIX + "/variablePay/getVariablePay";
	public static final String UPDATE_VARIABLE_PAY = PREFIX + "/variablePay/updateVariablePay";
	public static final String ADD_YEARLY_VARIABLE_PAY = PREFIX + "/variablePay/addYearlyVariablePay";
	public static final String UPDATE_YEARLY_VARIABLE_PAY = PREFIX + "/variablePay/updateYearlyVariablePay";;
	public static final String GET_YEARLY_VARIABLE_PAY = PREFIX + "/variablePay/getYearlyVariablePay";;




	//OLAP DATA
	public static final String SYNC_INVOICE_OLAP_DATA = PREFIX + "/olap/syncInvoiceData";
	public static final String FETCH_INTERNAL_INVOICE = PREFIX + "/olap/getInternalInvoice";
	public static final String SYNC_SALARY_OLAP_DATA = PREFIX + "/olap/syncSalaryData";
	public static final String SYNC_RESERVE_DATA = PREFIX + "/olap/syncReserveData";
	public static final String SYNC_DEDUCTION_DATA = PREFIX + "/olap/syncDeductionDta";
	public static final String SYNC_IC_DATA = PREFIX + "/olap/syncICData";

	//BuExpenses Controller
	public static final String GET_ALL_DEDUCTION_TYPE = PREFIX + "/buExpenses/getAllDeductionsType";
	public static final String EDIT_DEDUCTION_TYPE = PREFIX + "/buExpenses/editDeductionType";
	public static final String DELETE_DEDUCTION_TYPE = PREFIX+ "/buExpenses/deleteDeductionType";
	public static final String ADD_DEDUCTION_TYPE = PREFIX+"/buExpenses/addDeductionType";
	
	//BankLocation CRUD
	public static final String GET_ALL_BANK_LOCATIONS = PREFIX + "/getAllBankLocations";
	public static final String GET_BU_ALL_BANK_LOCATIONS = BU_PREFIX + "/getAllBankLocations";

	public static final String ADD_BANK_LOCATION = PREFIX + "/addBankLocation";
	public static final String DELETE_BANK_LOCATION = PREFIX + "/deleteBankLocation";
	public static final String GET_BANK_LOCATION = PREFIX +"/getBankLocationById";

	public static final String DELETE_BU_SPECIFIC_COST = PREFIX +"/deleteBuSpecificCost";
	public static final String GET_BU_SPECIFIC_COST = PREFIX +"/getBuSpecificCost";
	public static final String UPDATE_BU_SPECIFIC_COST = PREFIX +"/updateBuSpecificCost";
	public static final String ADD_BU_SPECIFIC_COST = PREFIX +"/addBuSpecificCost";
	public static final String DELETE_BU_SPECIFIC_TYPE = PREFIX +"/deleteBuSpecificType";
	public static final String GET_BU_SPECIFIC_TYPE = PREFIX +"/getBuSpecificType";
	public static final String UPDATE_BU_SPECIFIC_TYPE = PREFIX +"/updateBuSpecificType";
	public static final String ADD_BU_SPECIFIC_TYPE = PREFIX +"/addBuSpecificType";

	public static final String UPDATE_OVERALL_PL_LAST_YEARS = PREFIX + "/overAllPl/saveAllOverallPLData";

	public static final String UPDATE_OVERALL_PL = PREFIX + "/overAllPl/saveOverallPLData";
	public static final String UPDATE_COST_FORECASTING_DATA = PREFIX + "/costForecasting/saveCostForecastingData";
	public static final String GET_COST_FORECASTING_DATA = PREFIX + "/costForecasting/getCostForecastingData";

	public static final String OVERALL_PL_YEAR_WISE = PREFIX + "/overAllPl/getOverallPLData";
	public static final String DISPUTE_PL_YEAR_WISE = PREFIX + "/overAllPl/getYearWiseDisputeData";

	
}
