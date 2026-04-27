package io.alpa.pgnparser.controller;

import io.alpa.pgnparser.api.StudyApi;
import io.alpa.pgnparser.api.model.CreateLichessStudyRequest;
import io.alpa.pgnparser.api.model.CreateLichessStudyResponse;
import io.alpa.pgnparser.lichess.client.LichessStudiesClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for creating, updating and deleting studies. */
@RestController
@RequestMapping("/api")
public class StudyController implements StudyApi {

    private final LichessStudiesClient studiesClient;

    public StudyController(LichessStudiesClient studiesClient) {
        this.studiesClient = studiesClient;
    }

    @Override
    public ResponseEntity<CreateLichessStudyResponse> createStudyAndUploadPgn(CreateLichessStudyRequest request) {
        CreateLichessStudyResponse response = studiesClient.createStudyWithPgn(request);
        return ResponseEntity.ok(response);
    }


}
