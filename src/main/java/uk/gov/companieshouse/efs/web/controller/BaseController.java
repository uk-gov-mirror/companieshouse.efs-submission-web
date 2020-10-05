package uk.gov.companieshouse.efs.web.controller;

import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;

public interface BaseController {
    SubmissionApi getSubmission(String id);

    String getViewName();
}