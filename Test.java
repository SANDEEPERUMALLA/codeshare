package com.elavon.nextapi.domain.customer.loc.api.service;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.elavon.nextapi.commons.partyrelation.PartyRelationClient;
import com.elavon.nextapi.domain.customer.loc.api.model.AccountsSalesforceMappingObject;
import com.elavon.nextapi.domain.customer.loc.api.model.AddPersonProfile;
import com.elavon.nextapi.domain.customer.loc.api.model.Body;
import com.elavon.nextapi.domain.customer.loc.api.model.Body1;
import com.elavon.nextapi.domain.customer.loc.api.model.CreateCustomerLocationDetails;
import com.elavon.nextapi.domain.customer.loc.api.model.InlineResponse200;
import com.elavon.nextapi.domain.customer.loc.api.model.InlineResponse201;
import com.elavon.nextapi.domain.customer.loc.api.model.UpdateAccountsSalesforceMapping;
import com.elavon.nextapi.domain.customer.loc.api.model.InlineResponse200;
import com.elavon.nextapi.domain.customer.loc.api.model.UpdateCustomerLocationDetails;
import com.elavon.nextapi.domain.customer.loc.api.model.UpdateLeadSalesforceMapping;
import com.elavon.nextapi.domain.customer.loc.api.service.salesforce.SalesForceService;
@ExtendWith(MockitoExtension.class)
class CustomerLocationApiServiceTest {
    private static final String APPLICATION_ID = "123-123";
    private static final String AUTHORIZATION = "NGTALECH";
    @InjectMocks
    private CustomerLocationsApiService customerLocationApiService;
    @Mock
    private AccountsSalesforceMappingObject accountsSalesforceMappingObject;
    @Mock
    private Body body;
    @Mock
    private Body1 body1;
    @Mock
    private CreateCustomerLocationDetails createCustomerLocDetails;
    @Mock
    private PartyRelationClient partyRelationClient;
    @Mock
    private SalesForceService salesForceService;
    @Mock
    private UpdateAccountsSalesforceMapping updateAccountsSalesforceMapping;

    @Mock
    private Click2AcceptClient click2AcceptClient;
    @Mock
    private PartyRelationClient partyRelationClient;
    @Mock
    private SalesForceService salesforceService;

    @Test
    void shouldGetCustomerLocationDetails() {
        String file2 = "json/getcustloc-leadstatusresponse.json";
        String file3 = "json/getcustloc-accountdetails.json";
        JSONObject leadstsrespobj = new JSONObject();
        JSONObject acctdetlobj = new JSONObject();
        try {
            leadstsrespobj = new JSONObject(readFileAsString(file2));
            acctdetlobj = new JSONObject(readFileAsString(file3));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        when(salesForceService.getLeadStatusByApplicationId(APPLICATION_ID)).thenReturn(leadstsrespobj);
        when(salesForceService.getAccDetailsByApplicationId(APPLICATION_ID)).thenReturn(acctdetlobj);
        ResponseEntity<InlineResponse200> response = customerLocationApiService.getCustomerLocationDetails(AUTHORIZATION, APPLICATION_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    @Test
    void shouldCreateCustomerLocationDetails() {
        prepareData();
        ResponseEntity<InlineResponse201> responseEntity = customerLocationApiService.createCustomerLocationDetails(AUTHORIZATION, body);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }
    @Test
    void shouldUpdateCustomerLocationDetails() {
        when(salesForceService.getLeadStatusByApplicationId(APPLICATION_ID)).thenReturn(createCustomerUpdateResponse());
        ResponseEntity<?> result = customerLocationApiService.updateCustomerLocationDetails(AUTHORIZATION, APPLICATION_ID, body1);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
    private JSONObject createCustomerUpdateResponse() {
        JSONObject customerUpdateResponse = new JSONObject();
        JSONArray compositeResponses = new JSONArray(Arrays.asList(createCompositeResponse()));
        customerUpdateResponse.put("compositeResponse", compositeResponses);
        return customerUpdateResponse;
    }
    private JSONObject createCompositeResponse() {
        JSONObject compositeResponse = new JSONObject();
        compositeResponse.put("body", createResponseBody());
        return compositeResponse;
    }
    private JSONObject createResponseBody() {
        JSONObject responseBody = new JSONObject();
        responseBody.put("records", Arrays.asList(createRecord()));
        return responseBody;
    }
    private JSONObject createRecord() {
        JSONObject record = new JSONObject();
        record.put("IsConverted", "true");
        return record;
    }
    void prepareData() {
        String file1 = "json/postcustloc-createlead.json";
        CreateCustomerLocationDetails createCustomerLocDetails = new CreateCustomerLocationDetails();
        AddPersonProfile addPersonProfile = new AddPersonProfile();
        createCustomerLocDetails.setPersons(addPersonProfile);
        JSONObject createleadobj = null;
        try {
            createleadobj = new JSONObject(readFileAsString(file1));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        when(salesForceService.createLeadObject(ArgumentMatchers.<AccountsSalesforceMappingObject> any())).thenReturn(createleadobj);
        when(body.getCustomerLocation()).thenReturn(createCustomerLocDetails);
    }
    public String readFileAsString(String fileLoc) throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileLoc).getFile());
        return new String(Files.readAllBytes(file.toPath()));

    }

    @Test
    void shouldGetCustomerLocationDetailsWhenIsLeadConverted() {
        when(salesforceService.getLeadStatusByApplicationId(APPLICATION_ID)).thenReturn(createResponse());
        when(salesforceService.getAccDetailsByApplicationId(APPLICATION_ID)).thenReturn(createResponse());
        ResponseEntity<InlineResponse200> mockResponse = customerLocationApiService.getCustomerLocationDetails(AUTHORIZATION,
                APPLICATION_ID);
        verify(partyRelationClient).checkPartyRelation(AUTHORIZATION, APPLICATION_ID);
        verify(salesforceService).getLeadStatusByApplicationId(APPLICATION_ID);
        verify(salesforceService).getAccDetailsByApplicationId(APPLICATION_ID);
        verify(salesforceService, never()).getLeadDetailsByApplicationId(APPLICATION_ID);
        assertEquals(HttpStatus.OK, mockResponse.getStatusCode());
    }
    @Test
    void shouldGetCustomerLocationDetailsWhenIsNotLeadConverted() {
        when(salesforceService.getLeadStatusByApplicationId(APPLICATION_ID)).thenReturn(createErrorResponse());
        when(salesforceService.getLeadDetailsByApplicationId(APPLICATION_ID)).thenReturn(createErrorResponse());
        ResponseEntity<InlineResponse200> mockResponse = customerLocationApiService.getCustomerLocationDetails(AUTHORIZATION,
                APPLICATION_ID);
        verify(partyRelationClient).checkPartyRelation(AUTHORIZATION, APPLICATION_ID);
        verify(salesforceService).getLeadStatusByApplicationId(APPLICATION_ID);
        verify(salesforceService).getLeadDetailsByApplicationId(APPLICATION_ID);
        verify(salesforceService, never()).getAccDetailsByApplicationId(APPLICATION_ID);
        assertEquals(HttpStatus.OK, mockResponse.getStatusCode());
    }
    @Test
    void shouldUpdateCustomerLocationDetailsWhenIsLeadConverted() {
        when(body1.getCustomerLocation()).thenReturn(new UpdateCustomerLocationDetails());
        when(salesforceService.getLeadStatusByApplicationId(APPLICATION_ID))
                .thenReturn(createAccountLeadResponseBody());
        ResponseEntity<?> mockResponse = customerLocationApiService.updateCustomerLocationDetails(AUTHORIZATION,
                APPLICATION_ID, body1);
        verify(partyRelationClient).checkPartyRelation(AUTHORIZATION, APPLICATION_ID);
        verify(salesforceService).getLeadStatusByApplicationId(APPLICATION_ID);
        assertEquals(HttpStatus.NO_CONTENT, mockResponse.getStatusCode());
    }
    @Test
    void shouldUpdateCustomerLocationDetailsWhenIsNotLeadConverted() {
        when(body1.getCustomerLocation()).thenReturn(new UpdateCustomerLocationDetails());
        when(salesforceService.getLeadStatusByApplicationId(APPLICATION_ID))
                .thenReturn(createAccountLeadResponseBodyforelse());
        ResponseEntity<?> mockResponse = customerLocationApiService.updateCustomerLocationDetails(AUTHORIZATION,
                APPLICATION_ID, body1);
        verify(partyRelationClient).checkPartyRelation(AUTHORIZATION, APPLICATION_ID);
        verify(salesforceService).getLeadStatusByApplicationId(APPLICATION_ID);
        verify(salesforceService).updateLeadObjectByApplicationId(any(UpdateLeadSalesforceMapping.class),
                eq(APPLICATION_ID));
        assertEquals(HttpStatus.NO_CONTENT, mockResponse.getStatusCode());
    }
    private JSONObject createAccountLeadResponseBody() {
        JSONObject accountLeadResponse = new JSONObject();
        JSONArray accountLeadCompositeResponses = new JSONArray(Arrays.asList(createAccountCompositeResponse()));
        accountLeadResponse.put("compositeResponse", accountLeadCompositeResponses);
        return accountLeadResponse;
    }
    private JSONObject createAccountCompositeResponse() {
        JSONObject accountCompositeResponse = new JSONObject();
        accountCompositeResponse.put("body", createAccountResponseBody());
        return accountCompositeResponse;
    }
    private JSONObject createAccountResponseBody() {
        JSONObject accountResponseBody = new JSONObject();
        JSONArray accountRecordResponses = new JSONArray(Arrays.asList(createAccountRecord()));
        accountResponseBody.put("records", accountRecordResponses);
        return accountResponseBody;
    }
    private JSONObject createAccountRecordforelse() {
        JSONObject record = new JSONObject();
        record.put("IsConverted", Boolean.FALSE);
        return record;
    }
    private JSONObject createAccountLeadResponseBodyforelse() {
        JSONObject accountLeadResponse = new JSONObject();
        JSONArray accountLeadCompositeResponses = new JSONArray(Arrays.asList(createAccountCompositeResponseforelse()));
        accountLeadResponse.put("compositeResponse", accountLeadCompositeResponses);
        return accountLeadResponse;
    }
    private JSONObject createAccountCompositeResponseforelse() {
        JSONObject accountCompositeResponse = new JSONObject();
        accountCompositeResponse.put("body", createAccountResponseBodyforelse());
        return accountCompositeResponse;
    }
    private JSONObject createAccountResponseBodyforelse() {
        JSONObject accountResponseBody = new JSONObject();
        JSONArray accountRecordResponses = new JSONArray(Arrays.asList(createAccountRecordforelse()));
        accountResponseBody.put("records", accountRecordResponses);
        return accountResponseBody;
    }
    private JSONObject createAccountRecord() {
        JSONObject record = new JSONObject();
        record.put("IsConverted", Boolean.TRUE);
        return record;
    }
    private JSONObject createResponse() {
        JSONObject response = new JSONObject();
        JSONArray compositeResponses = new JSONArray(
                Arrays.asList(createCompositeResponse(), createCompositeAccountResponse(), createCompositeContactResponse(), createCompositeOppResponse()));
        response.put("compositeResponse", compositeResponses);
        return response;
    }
    private JSONObject createErrorResponse() {
        JSONObject response = new JSONObject();
        JSONArray compositeResponses = new JSONArray(
                Arrays.asList(createErrorCompositeResponse(), createCompositeAccountResponse(), createCompositeContactResponse(),
                        createCompositeOppResponse()));
        response.put("compositeResponse", compositeResponses);
        return response;
    }
    private JSONObject createCompositeContactResponse() {
        JSONObject accountCompositeResponse = new JSONObject();
        accountCompositeResponse.put("body", createContactBody());
        return accountCompositeResponse;
    }
    private JSONObject createContactBody() {
        JSONObject responseBody = new JSONObject();
        responseBody.put("totalSize", 1);
        responseBody.put("records", createContactRecord());
        return responseBody;
    }
    private JSONArray createContactRecord() {
        JSONObject records = new JSONObject();
        records.put("Will_contact_be_signing_the_contract__c", "true");
        JSONArray recordResponses = new JSONArray(Arrays.asList(records));
        return recordResponses;
    }
    private JSONObject createCompositeOppResponse() {
        JSONObject accountCompositeResponse = new JSONObject();
        accountCompositeResponse.put("body", createOppBody());
        return accountCompositeResponse;
    }
    private JSONObject createOppBody() {
        JSONObject responseBody = new JSONObject();
        responseBody.put("totalSize", 1);
        responseBody.put("records", createOppRecord());
        return responseBody;
    }
    private JSONArray createOppRecord() {
        JSONObject records = new JSONObject();
        records.put("Total_Monthly_V_MC_D_UP_Sales__c", 1000);
        JSONArray recordResponses = new JSONArray(Arrays.asList(records));
        return recordResponses;
    }
    private JSONObject createCompositeAccountResponse() {
        JSONObject accountCompositeResponse = new JSONObject();
        accountCompositeResponse.put("body", createAccountResponse1Body());
        return accountCompositeResponse;
    }
    private JSONObject createAccountResponse1Body() {
        JSONObject responseBody = new JSONObject();
        responseBody.put("Employer_ID_No_EIN__c", "789");
        responseBody.put("Personal_Guarantee_IP_Address__c", "xyz.com");
        responseBody.put("Personal_Guarantee_Device_ID__c", "xyz.com");
        responseBody.put("Personal_Guarantee_Time_Stamp__c", "xyz.com");
        responseBody.put("Personal_Guarantee_Email__c", "xyz.com");
        responseBody.put("Personal_Guarantee_Status__c", "xyz.com");
        responseBody.put("PG_DisclosureVersion__c", "signatureStatus");
        responseBody.put("Principal_Signature_IP_Address__c", "xyz.com");
        responseBody.put("Principal_Signature_Device_ID__c", "xyz.com");
        responseBody.put("Principal_Signature_Timestamp__c", "xyz.com");
        responseBody.put("Principal_Signature_Email__c", "xyz.com");
        responseBody.put("Principal_Signature_Status__c", "xyz.com");
        responseBody.put("PS_DisclosureVersion__c", "signatureStatus");
        responseBody.put("Terms_Conditions_IP_Address__c", "xyz.com");
        responseBody.put("Terms_Conditions_Device_ID__c", "xyz.com");
        responseBody.put("Terms_Conditions_TImestamp__c", "xyz.com");
        responseBody.put("Terms_Conditions_Email__c", "xyz.com");
        responseBody.put("Terms_Conditions_Status__c", "xyz.com");
        responseBody.put("TC_DisclosureVersion__c", "signatureStatus");
        responseBody.put("Tax_Id_Encrypted__c", "tezx");
        responseBody.put("Address_Classification__c", "tezx");
        responseBody.put("BillingStreet", "tezx");
        responseBody.put("Projected_Annual_Volume__c", "tezx");
        responseBody.put("Projected_Monthly_Transaction__c", "tezx");
        responseBody.put("Company_Annual_Turnover__c", "tezx");
        responseBody.put("Legal_Address_Classification__c", "tezx");
        responseBody.put("FundingReport__c", "tezx");
        responseBody.put("Funding_Currency_Code__c", "tezx");
        responseBody.put("Funding_Day__c", "tezx");
        responseBody.put("Funding_Currency_Description__c", "tezx");
        responseBody.put("Funding_Delay_Days__c", "tezx");
        responseBody.put("Funding_Frequency__c", "tezx");
        responseBody.put("Cannabis_Primary_Business_Purpose__c", "tezx");
        responseBody.put("Verification_Method__c", "tezx");
        responseBody.put("Special_Requirements_Customer_MCC_Status__c", "tezx");
        responseBody.put("Funding_Hold_days__c", "tezx");
        responseBody.put("Funding_Message_Text__c", "tezx");
        responseBody.put("Rebate_pricing_program__c", "tezx");
        responseBody.put("Client_Group__c", "tezx");
        responseBody.put("Country_of_Primary_Business_Operations__c", "tezx");
        responseBody.put("Country_of_Formation__c", "tezx");
        responseBody.put("MCC_Code__c", "tezx");
        responseBody.put("Imaging_Reference_Number__c", "tezx");
        responseBody.put("isClearCardReceived", false);
        responseBody.put("Pay_by_Chargeback__c", false);
        responseBody.put("AccountNumber", "tezx");
        responseBody.put("Portfolio__c", "tezx");
        responseBody.put("ABA_Routing__c", "tezx");
        responseBody.put("partnerChannelSourceId__c", "SFA – STATE FARM Agent Assisted");
        responseBody.put("partnerChannelAssociateId__c", "lkaddhcio5343ujkuedfgu");
        responseBody.put("partnerChannelBookofBusinessId__c", "ieqhyf9p53746354hdfkjhekuk");
        responseBody.put("partnerChannelTenure__c", "1");
        return responseBody;
    }
    private JSONObject createCompositeResponse() {
        JSONObject accountCompositeResponse = new JSONObject();
        accountCompositeResponse.put("body", createResponseBody());
        return accountCompositeResponse;
    }
    private JSONObject createErrorCompositeResponse() {
        JSONObject accountCompositeResponse = new JSONObject();
        accountCompositeResponse.put("body", createErrorResponseBody());
        return accountCompositeResponse;
    }
    private JSONObject createResponseBody() {
        JSONObject responseBody = new JSONObject();
        responseBody.put("records", createRecord());
        return responseBody;
    }
    private JSONObject createErrorResponseBody() {
        JSONObject responseBody = new JSONObject();
        responseBody.put("records", createErrorRecord());
        return responseBody;
    }
    private JSONArray createRecord() {
        JSONObject records = new JSONObject();
        records.put("IsConverted", true);
        JSONArray recordResponses = new JSONArray(Arrays.asList(records, records));
        return recordResponses;
    }
    private JSONArray createErrorRecord() {
        JSONObject records = new JSONObject();
        records.put("IsConverted", false);
        JSONArray recordResponses = new JSONArray(Arrays.asList(records, records));
        return recordResponses;
    }
}
    
    
