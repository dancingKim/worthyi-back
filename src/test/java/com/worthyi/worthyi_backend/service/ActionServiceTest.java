package com.worthyi.worthyi_backend.service;

import com.worthyi.worthyi_backend.common.ApiStatus;
import com.worthyi.worthyi_backend.model.dto.ActionDto;
import com.worthyi.worthyi_backend.model.dto.AdultActionDto;
import com.worthyi.worthyi_backend.model.dto.ApiResponse;
import com.worthyi.worthyi_backend.model.entity.AdultActionInstance;
import com.worthyi.worthyi_backend.model.entity.AdultActionTemplate;
import com.worthyi.worthyi_backend.model.entity.Avatar;
import com.worthyi.worthyi_backend.model.entity.ChildActionInstance;
import com.worthyi.worthyi_backend.model.entity.ChildActionTemplate;
import com.worthyi.worthyi_backend.model.entity.PlaceInstance;
import com.worthyi.worthyi_backend.model.entity.VillageInstance;
import com.worthyi.worthyi_backend.model.dto.ActionContentDto;
import com.worthyi.worthyi_backend.repository.AvatarRepository;
import com.worthyi.worthyi_backend.repository.ChildActionInstanceRepository;
import com.worthyi.worthyi_backend.repository.ChildActionTemplateRepository;
import com.worthyi.worthyi_backend.repository.PlaceInstanceRepository;
import com.worthyi.worthyi_backend.repository.VillageInstanceRepository;
import com.worthyi.worthyi_backend.repository.AdultActionInstanceRepository;
import com.worthyi.worthyi_backend.repository.AdultActionTemplateRepository;
import com.worthyi.worthyi_backend.security.PrincipalDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ActionServiceTest {

    @Mock
    private PlaceInstanceRepository placeInstanceRepository;
    @Mock
    private AvatarRepository avatarRepository;
    @Mock
    private ChildActionTemplateRepository childActionTemplateRepository;
    @Mock
    private AdultActionTemplateRepository adultActionTemplateRepository;
    @Mock
    private ChildActionInstanceRepository childActionInstanceRepository;
    @Mock
    private AdultActionInstanceRepository adultActionInstanceRepository;
    @Mock
    private VillageInstanceRepository villageInstanceRepository;

    @InjectMocks
    private ActionService actionService;

    // 각 테스트에서 사용할 더미 데이터
    private UUID dummyUserUUID;
    private String dummyUserId;
    private Avatar dummyAvatar;
    private VillageInstance dummyVillage;
    private PlaceInstance dummyPlace;
    private ChildActionTemplate dummyChildActionTemplate;
    private AdultActionTemplate dummyAdultActionTemplate;
    private ActionContentDto dummyActionContentDto;

    @BeforeEach
    public void setup() {
        dummyUserUUID = UUID.randomUUID();
        dummyUserId = dummyUserUUID.toString();

        // Avatar 더미 객체 생성 (실제 필드 및 메서드는 구현에 따라 달라질 수 있음)
        dummyAvatar = new Avatar();
        dummyAvatar.setAvatarId(1L);

        // VillageInstance 더미 객체 생성
        dummyVillage = new VillageInstance();
        dummyVillage.setVillageId(1L);

        // PlaceInstance 더미 객체 생성
        dummyPlace = new PlaceInstance();

        // ChildActionTemplate와 AdultActionTemplate 더미 객체 생성
        dummyChildActionTemplate = new ChildActionTemplate();
        // 필요에 따라 id 설정 (여기서는 1L)
        dummyAdultActionTemplate = new AdultActionTemplate();
        dummyActionContentDto = new ActionContentDto();
    }

    @Test
    public void testSaveChildActionSuccess() {
        // saveChildAction 성공 케이스 테스트

        // Arrange
        ActionDto.Request request = mock(ActionDto.Request.class);
        ChildActionInstance childActionInstance = new ChildActionInstance();
        when(request.toEntity(request)).thenReturn(childActionInstance);

        // ActionContentDto 객체 생성
        ActionContentDto actionContentDto = new ActionContentDto();
        actionContentDto.setText("test content"); // 필요한 필드 설정

        // ActionContentDto 객체 반환하도록 수정
        when(request.getContent()).thenReturn(actionContentDto);

        PrincipalDetails principalDetails = mock(PrincipalDetails.class);
        when(principalDetails.getName()).thenReturn(dummyUserId);

        // Avatar, Village, Place, Template 조회 모킹
        when(avatarRepository.findByUserUserId(dummyUserUUID)).thenReturn(Optional.of(dummyAvatar));
        when(villageInstanceRepository.findByUserUserId(dummyUserUUID)).thenReturn(Optional.of(dummyVillage));
        when(placeInstanceRepository.findByVillageInstance_VillageId(dummyVillage.getVillageId()))
                .thenReturn(Optional.of(dummyPlace));
        when(childActionTemplateRepository.findById(1L))
                .thenReturn(Optional.of(dummyChildActionTemplate));

        // 저장 결과 모킹
        ChildActionInstance savedInstance = new ChildActionInstance();
        savedInstance.setChildActionInstanceId(1L);
        when(childActionInstanceRepository.save(any(ChildActionInstance.class))).thenReturn(savedInstance);

        // Act
        ApiResponse<ActionDto.Response> response = actionService.saveChildAction(request, principalDetails);

        // Assert
        assertTrue(response.isSuccess(), "응답은 성공이어야 합니다.");
    }

    @Test
    public void testSaveChildActionAvatarNotFound() {
        // Avatar 조회 실패 케이스 테스트

        // Arrange
        ActionDto.Request request = mock(ActionDto.Request.class);
        when(request.toEntity(request)).thenReturn(new ChildActionInstance());

        PrincipalDetails principalDetails = mock(PrincipalDetails.class);
        when(principalDetails.getName()).thenReturn(dummyUserId);

        when(avatarRepository.findByUserUserId(dummyUserUUID)).thenReturn(Optional.empty());

        // Act
        ApiResponse<ActionDto.Response> response = actionService.saveChildAction(request, principalDetails);

        // Assert
        assertFalse(response.isSuccess(), "응답은 실패여야 합니다.");
        assertEquals(ApiStatus.AVATAR_NOT_FOUND.getMessage(), response.getMessage());
    }

    @Test
    public void testSaveAdultActionSuccess() {
        // saveAdultAction 성공 케이스 테스트

        // Arrange
        Long childActionId = 1L;
        AdultActionDto.Request request = mock(AdultActionDto.Request.class);
        AdultActionInstance adultActionInstance = new AdultActionInstance();
        when(request.toEntity(request)).thenReturn(adultActionInstance);

        PrincipalDetails principalDetails = mock(PrincipalDetails.class);
        when(principalDetails.getName()).thenReturn(dummyUserId);

        // ChildAction 조회 모킹
        ChildActionInstance childActionInstance = new ChildActionInstance();
        childActionInstance.setChildActionInstanceId(childActionId);
        when(childActionInstanceRepository.findById(childActionId)).thenReturn(Optional.of(childActionInstance));

        // AdultActionTemplate 조회 모킹
        when(adultActionTemplateRepository.findById(1L))
                .thenReturn(Optional.of(dummyAdultActionTemplate));

        // 저장 결과 모킹
        AdultActionInstance savedInstance = new AdultActionInstance();
        savedInstance.setAdultActionInstanceId(1L);
        when(adultActionInstanceRepository.save(any(AdultActionInstance.class))).thenReturn(savedInstance);

        // Act
        ApiResponse<AdultActionDto.Response> response = actionService.saveAdultAction(childActionId, request, principalDetails);

        // Assert
        assertTrue(response.isSuccess(), "응답은 성공이어야 합니다.");
    }

    @Test
    public void testSaveAdultActionChildActionNotFound() {
        // saveAdultAction에서 ChildAction이 존재하지 않는 경우 테스트

        // Arrange
        Long childActionId = 1L;
        AdultActionDto.Request request = mock(AdultActionDto.Request.class);
        when(request.toEntity(request)).thenReturn(new AdultActionInstance());

        PrincipalDetails principalDetails = mock(PrincipalDetails.class);
        when(principalDetails.getName()).thenReturn(dummyUserId);

        when(childActionInstanceRepository.findById(childActionId)).thenReturn(Optional.empty());

        // Act
        ApiResponse<AdultActionDto.Response> response = actionService.saveAdultAction(childActionId, request, principalDetails);

        // Assert
        assertFalse(response.isSuccess(), "응답은 실패여야 합니다.");
        assertEquals(ApiStatus.CHILD_ACTION_NOT_FOUND.getMessage(), response.getMessage());
    }

    @Test
    public void testGetChildActionsByDateSuccess() {
        // getChildActionsByDate 성공 케이스 테스트

        // Arrange
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        when(avatarRepository.findByUserUserId(dummyUserUUID))
                .thenReturn(Optional.of(dummyAvatar));

        ChildActionInstance instance = new ChildActionInstance();
        instance.setChildActionInstanceId(1L);
        when(childActionInstanceRepository.findAllByDateAndAvatarId(dummyAvatar.getAvatarId(), startOfDay, endOfDay))
                .thenReturn(Collections.singletonList(instance));

        // Act
        // getChildActionsByDate 내부에서는 UUID.fromString(userId)가 사용되므로, dummyUserId 이용
        var responses = actionService.getChildActionsByDate(dummyUserId, today);

        // Assert
        assertEquals(1, responses.size(), "결과 리스트의 크기는 1이어야 합니다.");
    }

    @Test
    public void testGetActionLogsSuccess() {
        // getActionLogs 성공 케이스 테스트

        // Arrange
        LocalDate date = LocalDate.now();
        PrincipalDetails principalDetails = mock(PrincipalDetails.class);
        when(principalDetails.getName()).thenReturn(dummyUserId);

        when(avatarRepository.findByUserUserId(dummyUserUUID))
                .thenReturn(Optional.of(dummyAvatar));

        // getChildActionsByDate 메서드 호출 부분 모킹
        ChildActionInstance instance = new ChildActionInstance();
        instance.setChildActionInstanceId(1L);
        when(childActionInstanceRepository.findAllByDateAndAvatarId(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(instance));

        // 통계 계산을 위한 countDistinctDatesByAvatarIdAndDateBetween 모킹
        when(childActionInstanceRepository.countDistinctDatesByAvatarIdAndDateBetween(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(5);

        // Act
        var response = actionService.getActionLogs(principalDetails, date);

        // Assert
        assertNotNull(response, "응답은 null이 아니어야 합니다.");
        assertEquals(5, response.getWeeklyCount(), "주간 카운트는 5여야 합니다.");
        assertEquals(5, response.getMonthlyCount(), "월간 카운트는 5여야 합니다.");
        assertEquals(5, response.getYearlyCount(), "연간 카운트는 5여야 합니다.");
    }

    @Test
    public void testDeleteChildActionSuccess() {
        // deleteChildAction 성공 케이스 테스트

        // Arrange
        Long childActionId = 1L;
        when(avatarRepository.findByUserUserId(dummyUserUUID))
                .thenReturn(Optional.of(dummyAvatar));

        ChildActionInstance childAction = new ChildActionInstance();
        childAction.setChildActionInstanceId(childActionId);
        childAction.setAvatarId(dummyAvatar.getAvatarId());
        when(childActionInstanceRepository.findById(childActionId))
                .thenReturn(Optional.of(childAction));

        // Act
        ApiResponse<Void> response = actionService.deleteChildAction(childActionId, dummyUserId);

        // Assert
        assertTrue(response.isSuccess(), "응답은 성공이어야 합니다.");
        verify(childActionInstanceRepository, times(1)).delete(childAction);
    }

    @Test
    public void testDeleteChildActionNotAuthorized() {
        // deleteChildAction 삭제 권한이 없는 경우 테스트

        // Arrange
        Long childActionId = 1L;
        when(avatarRepository.findByUserUserId(dummyUserUUID))
                .thenReturn(Optional.of(dummyAvatar));

        ChildActionInstance childAction = new ChildActionInstance();
        childAction.setChildActionInstanceId(childActionId);
        // 다른 avatarId로 설정하여 권한 미일치 처리
        childAction.setAvatarId(dummyAvatar.getAvatarId() + 1);
        when(childActionInstanceRepository.findById(childActionId))
                .thenReturn(Optional.of(childAction));

        // Act
        ApiResponse<Void> response = actionService.deleteChildAction(childActionId, dummyUserId);

        // Assert
        assertFalse(response.isSuccess(), "응답은 실패여야 합니다.");
        assertEquals(ApiStatus.NOT_AUTHORIZED_TO_DELETE.getMessage(), response.getMessage());
    }

    @Test
    public void testDeleteAdultActionSuccess() {
        // deleteAdultAction 성공 케이스 테스트

        // Arrange
        Long childActionId = 1L;
        Long adultActionId = 1L;

        // ChildAction 조회 모킹
        ChildActionInstance childAction = new ChildActionInstance();
        childAction.setChildActionInstanceId(childActionId);
        when(childActionInstanceRepository.findById(childActionId))
                .thenReturn(Optional.of(childAction));

        // AdultAction 조회 및 내부 연관관계 설정 모킹
        AdultActionInstance adultAction = new AdultActionInstance();
        adultAction.setAdultActionInstanceId(adultActionId);
        adultAction.setChildActionInstance(childAction);
        adultAction.setUserId(dummyUserUUID);
        when(adultActionInstanceRepository.findById(adultActionId))
                .thenReturn(Optional.of(adultAction));

        // Act
        ApiResponse<Void> response = actionService.deleteAdultAction(childActionId, adultActionId, dummyUserId);

        // Assert
        assertTrue(response.isSuccess(), "응답은 성공이어야 합니다.");
        verify(adultActionInstanceRepository, times(1)).delete(adultAction);
    }

    @Test
    public void testDeleteAdultActionInvalidRelationship() {
        // deleteAdultAction에서 ChildAction과 AdultAction의 관계가 올바르지 않은 경우 테스트

        // Arrange
        Long childActionId = 1L;
        Long adultActionId = 1L;

        // 올바른 ChildAction 조회 모킹
        ChildActionInstance childAction = new ChildActionInstance();
        childAction.setChildActionInstanceId(childActionId);
        when(childActionInstanceRepository.findById(childActionId))
                .thenReturn(Optional.of(childAction));

        // 다른 ChildAction과 연관된 AdultAction 모킹
        ChildActionInstance anotherChildAction = new ChildActionInstance();
        anotherChildAction.setChildActionInstanceId(2L);
        AdultActionInstance adultAction = new AdultActionInstance();
        adultAction.setAdultActionInstanceId(adultActionId);
        adultAction.setChildActionInstance(anotherChildAction);
        adultAction.setUserId(dummyUserUUID);
        when(adultActionInstanceRepository.findById(adultActionId))
                .thenReturn(Optional.of(adultAction));

        // Act
        ApiResponse<Void> response = actionService.deleteAdultAction(childActionId, adultActionId, dummyUserId);

        // Assert
        assertFalse(response.isSuccess(), "응답은 실패여야 합니다.");
        assertEquals(ApiStatus.INVALID_ACTION_RELATIONSHIP.getMessage(), response.getMessage());
    }

    @Test
    public void testDeleteAdultActionNotAuthorized() {
        // deleteAdultAction에서 삭제 권한이 없는 경우 테스트

        // Arrange
        Long childActionId = 1L;
        Long adultActionId = 1L;

        // ChildAction 조회 모킹
        ChildActionInstance childAction = new ChildActionInstance();
        childAction.setChildActionInstanceId(childActionId);
        when(childActionInstanceRepository.findById(childActionId))
                .thenReturn(Optional.of(childAction));

        // 권한이 다른 사용자로 설정된 AdultAction 조회 모킹
        AdultActionInstance adultAction = new AdultActionInstance();
        adultAction.setAdultActionInstanceId(adultActionId);
        adultAction.setChildActionInstance(childAction);
        adultAction.setUserId(UUID.randomUUID());
        when(adultActionInstanceRepository.findById(adultActionId))
                .thenReturn(Optional.of(adultAction));

        // Act
        ApiResponse<Void> response = actionService.deleteAdultAction(childActionId, adultActionId, dummyUserId);

        // Assert
        assertFalse(response.isSuccess(), "응답은 실패여야 합니다.");
        assertEquals(ApiStatus.NOT_AUTHORIZED_TO_DELETE.getMessage(), response.getMessage());
    }
} 