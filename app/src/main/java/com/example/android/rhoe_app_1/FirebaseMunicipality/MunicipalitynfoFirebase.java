package com.example.android.rhoe_app_1.FirebaseMunicipality;

/**
 * Created by User on 6/03/2018.
 */

public class MunicipalitynfoFirebase {

    public String MunAddress, MunBank, MunBankIBAN, MunDepartment, MunEmail, MunName, MunPayAddress1, MunPayAddress2, MunPayAddress3, MunPayName, MunPostNum, MunRegion, MunTel1, MunTel2;

    public MunicipalitynfoFirebase(){

    }

    public MunicipalitynfoFirebase(String munAddress, String munBank, String munBankIBAN, String munDepartment, String munEmail, String munName, String munPayAddress1, String munPayAddress2, String munPayAddress3, String munPayName, String munPostNum, String munRegion, String munTel1, String munTel2) {
        MunAddress = munAddress;
        MunBank = munBank;
        MunBankIBAN = munBankIBAN;
        MunDepartment = munDepartment;
        MunEmail = munEmail;
        MunName = munName;
        MunPayAddress1 = munPayAddress1;
        MunPayAddress2 = munPayAddress2;
        MunPayAddress3 = munPayAddress3;
        MunPayName = munPayName;
        MunPostNum = munPostNum;
        MunRegion = munRegion;
        MunTel1 = munTel1;
        MunTel2 = munTel2;
    }
}
