package org.openmrs.module.aijar.htmlformentry;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.module.aijar.metadata.core.Programs;
import org.openmrs.module.htmlformentry.FormEntryContext;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;

/**
 * Tests patient enrollment into the TB program
 */
public class TBProgramEnrollmentPostSubmissionActionTest extends BaseModuleWebContextSensitiveTest {
	
	@Test
	public void shouldEnrollPatientInTBProgramOnlyWhenExpectedConditionsAreFullFilled() {
		
		Patient patient = new Patient(2);
		Encounter encounter = new Encounter();
		encounter.setEncounterDatetime(new Date());
		
		TBProgramEnrollmentPostSubmissionAction postSubmissionAction = new TBProgramEnrollmentPostSubmissionAction();
		
		FormEntrySession formEntrySession = mock(FormEntrySession.class);
		FormEntryContext formEntryContext = mock(FormEntryContext.class);
		
		when(formEntrySession.getContext()).thenReturn(formEntryContext);
		when(formEntrySession.getPatient()).thenReturn(patient);
		when(formEntrySession.getEncounter()).thenReturn(encounter);
		
		ProgramWorkflowService programWorkflowService = Context.getService(ProgramWorkflowService.class);
		Program tbProgram = new Program(Programs.TB_PROGRAM.name());
		tbProgram.setConcept(new Concept(3));
		tbProgram.setUuid(Programs.TB_PROGRAM.uuid());
		programWorkflowService.saveProgram(tbProgram);
		
		List<PatientProgram> programs = programWorkflowService.getPatientPrograms(patient, tbProgram, null, null, null, null,
		    false);
		Assert.assertEquals(0, programs.size());
		
		//try enroll in vew mode
		when(formEntrySession.getContext().getMode()).thenReturn(FormEntryContext.Mode.VIEW);
		postSubmissionAction.applyAction(formEntrySession);
		programs = programWorkflowService.getPatientPrograms(patient, tbProgram, null, null, null, null, false);
		Assert.assertEquals(0, programs.size()); //should not enroll in view mode
		
		//try enroll in edit mode
		when(formEntrySession.getContext().getMode()).thenReturn(FormEntryContext.Mode.EDIT);
		postSubmissionAction.applyAction(formEntrySession);
		programs = programWorkflowService.getPatientPrograms(patient, tbProgram, null, null, null, null, false);
		Assert.assertEquals(0, programs.size()); //should not enroll in edit mode
		
		//try enroll in enter mode
		when(formEntrySession.getContext().getMode()).thenReturn(FormEntryContext.Mode.ENTER);
		postSubmissionAction.applyAction(formEntrySession);
		programs = programWorkflowService.getPatientPrograms(patient, tbProgram, null, null, null, null, false);
		Assert.assertEquals(1, programs.size()); //should enroll in enter mode
		
		//try enroll again for the same patient and program in enter mode
		postSubmissionAction.applyAction(formEntrySession);
		programs = programWorkflowService.getPatientPrograms(patient, tbProgram, null, null, null, null, false);
		Assert.assertEquals(1, programs.size()); //should not do duplicate enrollment
	}
}