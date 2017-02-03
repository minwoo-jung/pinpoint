package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.dao.NssAuthDao;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
@RunWith(MockitoJUnitRunner.class)
public class NssAuthServiceTest {

    private final NssAuthDao nssAuthDao = mock(NssAuthDao.class);

    private final NssAuthService nssAuthService = new NssAuthServiceImpl(this.nssAuthDao);

    @Test
    public void nullAuthorizedPrefixes_shouldReturnEmptyList() {
        // Given
        when(nssAuthDao.selectAuthorizedPrefix()).thenReturn(null);
        // When
        Collection<String> authorizedPrefixes = nssAuthService.getAuthorizedPrefixes();
        // Then
        Assert.assertTrue(authorizedPrefixes.isEmpty());
    }

    @Test
    public void emptyAuthorizedPrefix_shouldReturnEmptyList() {
        // Given
        when(nssAuthDao.selectAuthorizedPrefix()).thenReturn(Collections.<String>emptyList());
        // When
        Collection<String> authorizedPrefixes = nssAuthService.getAuthorizedPrefixes();
        // Then
        Assert.assertTrue(authorizedPrefixes.isEmpty());
    }

    @Test
    public void nullOverrideUserId_shouldReturnEmptySet() {
        // Given
        when(nssAuthDao.selectOverrideUserId()).thenReturn(null);
        // When
        Collection<String> overrideUserIds = nssAuthService.getOverrideUserIds();
        // Then
        Assert.assertTrue(overrideUserIds.isEmpty());
    }

    @Test
    public void emptyOverrideUserId_shouldReturnEmptySet() {
        // Given
        when(nssAuthDao.selectOverrideUserId()).thenReturn(Collections.<String>emptyList());
        // When
        Collection<String> overrideUserIds = nssAuthService.getOverrideUserIds();
        // Then
        Assert.assertTrue(overrideUserIds.isEmpty());
    }
}
