package com.mobo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mobo.entity.AgenciesEntity;
import com.mobo.entity.MediatorProfilesEntity;
import com.mobo.entity.enums.AgencyStatus;
import com.mobo.entity.enums.MediatorStatus;
import com.mobo.repository.AgenciesRepository;
import com.mobo.repository.MediatorProfilesRepository;
import com.mobo.service.impl.LineageServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LineageServiceImplTest {

  @Mock private AgenciesRepository agenciesRepository;
  @Mock private MediatorProfilesRepository mediatorProfilesRepository;
  @InjectMocks private LineageServiceImpl service;

  @BeforeEach
  void clearCache() {
    service.clearCache();
  }

  // ── isAgencyActive ──────────────────────────────────────────────────────

  @Test
  void isAgencyActive_activeAgency_returnsTrue() {
    AgenciesEntity agency = new AgenciesEntity();
    agency.setStatus(AgencyStatus.active);
    when(agenciesRepository.findByAgencyCodeAndIsDeletedFalse("AGC1"))
        .thenReturn(Optional.of(agency));

    assertThat(service.isAgencyActive("AGC1")).isTrue();
  }

  @Test
  void isAgencyActive_inactiveAgency_returnsFalse() {
    AgenciesEntity agency = new AgenciesEntity();
    agency.setStatus(AgencyStatus.suspended);
    when(agenciesRepository.findByAgencyCodeAndIsDeletedFalse("AGC2"))
        .thenReturn(Optional.of(agency));

    assertThat(service.isAgencyActive("AGC2")).isFalse();
  }

  @Test
  void isAgencyActive_notFound_returnsFalse() {
    when(agenciesRepository.findByAgencyCodeAndIsDeletedFalse("MISSING"))
        .thenReturn(Optional.empty());

    assertThat(service.isAgencyActive("MISSING")).isFalse();
  }

  @Test
  void isAgencyActive_cacheHit_callsRepositoryOnce() {
    AgenciesEntity agency = new AgenciesEntity();
    agency.setStatus(AgencyStatus.active);
    when(agenciesRepository.findByAgencyCodeAndIsDeletedFalse("AGC3"))
        .thenReturn(Optional.of(agency));

    service.isAgencyActive("AGC3");
    service.isAgencyActive("AGC3"); // second call should hit cache

    verify(agenciesRepository, times(1)).findByAgencyCodeAndIsDeletedFalse("AGC3");
  }

  // ── isMediatorActive ────────────────────────────────────────────────────

  @Test
  void isMediatorActive_activeMediator_returnsTrue() {
    MediatorProfilesEntity mediator = new MediatorProfilesEntity();
    mediator.setStatus(MediatorStatus.active);
    when(mediatorProfilesRepository.findByMediatorCode("MED1")).thenReturn(Optional.of(mediator));

    assertThat(service.isMediatorActive("MED1")).isTrue();
  }

  @Test
  void isMediatorActive_inactiveMediator_returnsFalse() {
    MediatorProfilesEntity mediator = new MediatorProfilesEntity();
    mediator.setStatus(MediatorStatus.suspended);
    when(mediatorProfilesRepository.findByMediatorCode("MED2")).thenReturn(Optional.of(mediator));

    assertThat(service.isMediatorActive("MED2")).isFalse();
  }

  @Test
  void isMediatorActive_cacheHit_callsRepositoryOnce() {
    MediatorProfilesEntity mediator = new MediatorProfilesEntity();
    mediator.setStatus(MediatorStatus.active);
    when(mediatorProfilesRepository.findByMediatorCode("MED3")).thenReturn(Optional.of(mediator));

    service.isMediatorActive("MED3");
    service.isMediatorActive("MED3");

    verify(mediatorProfilesRepository, times(1)).findByMediatorCode("MED3");
  }

  @Test
  void clearCache_forcesRepositoryCallOnNextLookup() {
    AgenciesEntity agency = new AgenciesEntity();
    agency.setStatus(AgencyStatus.active);
    when(agenciesRepository.findByAgencyCodeAndIsDeletedFalse("AGC4"))
        .thenReturn(Optional.of(agency));

    service.isAgencyActive("AGC4"); // populates cache
    service.clearCache();
    service.isAgencyActive("AGC4"); // should hit repo again

    verify(agenciesRepository, times(2)).findByAgencyCodeAndIsDeletedFalse("AGC4");
  }

  // ── listMediatorCodesForAgency ──────────────────────────────────────────

  @Test
  void listMediatorCodesForAgency_returnsCodes() {
    MediatorProfilesEntity m1 = new MediatorProfilesEntity();
    m1.setMediatorCode("M001");
    MediatorProfilesEntity m2 = new MediatorProfilesEntity();
    m2.setMediatorCode("M002");
    when(mediatorProfilesRepository.findAllByParentAgencyCodeAndIsDeletedFalse("AGC5"))
        .thenReturn(List.of(m1, m2));

    List<String> codes = service.listMediatorCodesForAgency("AGC5");

    assertThat(codes).containsExactly("M001", "M002");
  }

  @Test
  void listMediatorCodesForAgency_cacheHit_callsRepositoryOnce() {
    MediatorProfilesEntity m = new MediatorProfilesEntity();
    m.setMediatorCode("M003");
    when(mediatorProfilesRepository.findAllByParentAgencyCodeAndIsDeletedFalse("AGC6"))
        .thenReturn(List.of(m));

    service.listMediatorCodesForAgency("AGC6");
    service.listMediatorCodesForAgency("AGC6");

    verify(mediatorProfilesRepository, times(1)).findAllByParentAgencyCodeAndIsDeletedFalse("AGC6");
  }
}
