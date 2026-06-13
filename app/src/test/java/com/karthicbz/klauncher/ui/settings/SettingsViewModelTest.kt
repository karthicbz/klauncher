package com.karthicbz.klauncher.ui.settings

import app.cash.turbine.test
import com.karthicbz.klauncher.data.db.CategoryDao
import com.karthicbz.klauncher.data.model.CategoryEntity
import com.karthicbz.klauncher.repository.AppRepository
import com.karthicbz.klauncher.repository.ThemeRepository
import com.karthicbz.klauncher.repository.UserPreferencesRepository
import com.karthicbz.klauncher.repository.WallpaperSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val categoryDao: CategoryDao = mockk(relaxed = true)
    private val appRepository: AppRepository = mockk(relaxed = true)
    private val themeRepository: ThemeRepository = mockk(relaxed = true)
    private val userPreferencesRepository: UserPreferencesRepository = mockk(relaxed = true)

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { categoryDao.getAllCategories() } returns MutableStateFlow(emptyList())
        every { appRepository.getCategoriesWithAllApps() } returns MutableStateFlow(emptyMap())
        every { userPreferencesRepository.wallpaperImageUrl } returns MutableStateFlow(null)
        every { userPreferencesRepository.wallpaperSource } returns MutableStateFlow(WallpaperSource.NONE)
        every { userPreferencesRepository.wallpaperColor } returns MutableStateFlow(null)
        every { userPreferencesRepository.latitude } returns MutableStateFlow(0f)
        every { userPreferencesRepository.longitude } returns MutableStateFlow(0f)
        every { userPreferencesRepository.pixabayCategory } returns MutableStateFlow("nature")

        viewModel = SettingsViewModel(
            categoryDao = categoryDao,
            themeRepository = themeRepository,
            appRepository = appRepository,
            userPreferencesRepository = userPreferencesRepository
        )
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has no wallpaper status`() = runTest {
        viewModel.wallpaperStatus.test {
            assert(awaitItem() == null)
            cancel()
        }
    }

    @Test
    fun `initial state is not loading wallpaper`() = runTest {
        viewModel.isLoadingWallpaper.test {
            assert(!awaitItem())
            cancel()
        }
    }

    @Test
    fun `setWallpaperColor clears image url and sets source to SOLID_COLOR`() {
        viewModel.setWallpaperColor("#000000")

        verify { userPreferencesRepository.setWallpaperColor("#000000") }
    }

    @Test
    fun `setWallpaperImageUrl updates image url with source`() {
        viewModel.setWallpaperImageUrl("https://example.com/img.jpg")

        verify { userPreferencesRepository.setWallpaperImageUrl("https://example.com/img.jpg", WallpaperSource.LOCAL_IMAGE) }
    }

    @Test
    fun `pixabay categories come from PixabayApi`() {
        assert(viewModel.pixabayCategories.contains("nature"))
        assert(viewModel.pixabayCategories.contains("backgrounds"))
        assert(viewModel.pixabayCategories.contains("animals"))
        assert(viewModel.pixabayCategories.size == 20)
    }

    @Test
    fun `setPixabayCategory delegates to repository`() {
        viewModel.setPixabayCategory("animals")

        verify { userPreferencesRepository.setPixabayCategory("animals") }
    }

    @Test
    fun `setWallpaperSource delegates to repository`() {
        viewModel.setWallpaperSource(WallpaperSource.BING)

        verify { userPreferencesRepository.setWallpaperSource(WallpaperSource.BING) }
    }

    @Test
    fun `fetchBingWallpaper sets loading to true then false`() = runTest {
        coEvery { categoryDao.getAllCategories() } returns MutableStateFlow(emptyList())

        viewModel.isLoadingWallpaper.test {
            assert(!awaitItem())

            viewModel.fetchBingWallpaper()
            advanceUntilIdle()

            assert(awaitItem())
            assert(!awaitItem())
            cancel()
        }
    }

    @Test
    fun `fetchBingWallpaper sets error status on exception`() = runTest {
        every { userPreferencesRepository.setWallpaperImageUrl(any(), any()) } throws RuntimeException("Network error")

        viewModel.wallpaperStatus.test {
            assert(awaitItem() == null)

            viewModel.fetchBingWallpaper()
            advanceUntilIdle()

            val status = awaitItem()
            assert(status != null && status!!.contains("error", ignoreCase = true))
            cancel()
        }
    }

    @Test
    fun `addCategory inserts category with next position`() = runTest {
        coEvery { categoryDao.getMaxPosition() } returns 2
        coEvery { categoryDao.insertCategory(any()) } returns 1L

        viewModel.addCategory("Movies")
        advanceUntilIdle()

        coVerify { categoryDao.insertCategory(match { it.name == "Movies" && it.position == 3 }) }
    }

    @Test
    fun `addCategory inserts category at position 0 when no categories exist`() = runTest {
        coEvery { categoryDao.getMaxPosition() } returns null
        coEvery { categoryDao.insertCategory(any()) } returns 1L

        viewModel.addCategory("Games")
        advanceUntilIdle()

        coVerify { categoryDao.insertCategory(match { it.name == "Games" && it.position == 0 }) }
    }

    @Test
    fun `renameCategory delegates to dao`() = runTest {
        val category = CategoryEntity(id = 1, name = "Old", position = 0, isSystem = false)
        coEvery { categoryDao.updateCategory(any()) } just runs

        viewModel.renameCategory(category, "New")
        advanceUntilIdle()

        coVerify { categoryDao.updateCategory(match { it.name == "New" && it.id == 1L }) }
    }

    @Test
    fun `deleteCategory skips system categories`() = runTest {
        val systemCategory = CategoryEntity(id = 1, name = "Apps", position = 0, isSystem = true)
        coEvery { categoryDao.deleteCategory(any()) } just runs

        viewModel.deleteCategory(systemCategory)
        advanceUntilIdle()

        coVerify(exactly = 0) { categoryDao.deleteCategory(any()) }
    }

    @Test
    fun `deleteCategory deletes user categories`() = runTest {
        val userCategory = CategoryEntity(id = 2, name = "Games", position = 1, isSystem = false)
        coEvery { categoryDao.deleteCategory(any()) } just runs

        viewModel.deleteCategory(userCategory)
        advanceUntilIdle()

        coVerify(exactly = 1) { categoryDao.deleteCategory(userCategory) }
    }
}
