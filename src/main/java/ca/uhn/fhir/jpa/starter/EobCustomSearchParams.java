package ca.uhn.fhir.jpa.starter;

import org.hl7.fhir.r4.model.SearchParameter;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.SearchParameter.XPathUsageType;

public class EobCustomSearchParams {

  public SearchParameter coverageSearchParameter() {
    SearchParameter coverage = new SearchParameter();
    coverage.setName("ExplanationOfBenefit_Coverage");
    coverage.setCode("coverage");
    coverage.setDescription("The plan under which the claim was adjudicated");
    coverage.setUrl("http://hl7.org/fhir/us/carin-bb/SearchParameter/explanationofbenefit-coverage");
    coverage.setStatus(PublicationStatus.ACTIVE);
    coverage.addBase("ExplanationOfBenefit");
    coverage.setType(SearchParamType.REFERENCE);
    coverage.setXpathUsage(XPathUsageType.NORMAL);
    coverage.setXpath("f:ExplanationOfBenefit/f:insurance/f:coverage");
    coverage.setExpression("ExplanationOfBenefit.insurance.coverage");
    coverage.addTarget("Coverage");
    return coverage;
  }

  public SearchParameter careTeamSearchParameter() {
    SearchParameter careteam = new SearchParameter();
    return careteam;
  }
}
