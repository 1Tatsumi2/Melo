using MusicApp.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using System.Data.Entity; // Nếu bạn đang sử dụng Entity Framework

namespace MusicApp.Controllers
{
    public class HomeController : Controller
    {
        private readonly DAPMMainEntities db = new DAPMMainEntities();

		public ActionResult TrangChu()
		{
			// Lấy 8 playlist đầu tiên (danh sách chính)
			var playlists = db.PlayLists.Include(p => p.Account).Take(8).ToList();

			// Lấy các playlist theo category, ví dụ như category với ID = 1
			var categoryId = 1; // ID của category bạn muốn lấy
			var categoryPlaylists = db.PlayLists
									   .Where(p => p.Ma_The_Loai == categoryId)
									   .Include(p => p.Account)
									   .Include(p => p.Song_Play.Select(sp => sp.Song.Singer)) // Include songs and their singers
									   .ToList();

			var topMixesCategoryId = 2;
			// Retrieve playlists belonging to the "Top mixes" category
			var topMixesPlaylists = db.PlayLists
									   .Where(p => p.Ma_The_Loai == topMixesCategoryId)
									   .Include(p => p.Song_Play.Select(sp => sp.Song.Singer)) // Include related data if needed
									   .ToList();

			var popularRadioCategoryId = 3;
			// Retrieve playlists belonging to the "Top mixes" category
			var popularRadioPlaylists = db.PlayLists
									   .Where(p => p.Ma_The_Loai == popularRadioCategoryId)
									   .Include(p => p.Song_Play.Select(sp => sp.Song.Singer)) // Include related data if needed
									   .ToList();

			// Pass the playlists to the ViewBag
			ViewBag.TopMixesPlaylists = topMixesPlaylists;
			ViewBag.PopularRadioPlaylists = popularRadioPlaylists;
			ViewBag.Playlists = playlists;
			ViewBag.CategoryPlaylists = categoryPlaylists;


			return View();
		}


		public ActionResult PlayListDetail(int id)
		{
			// Lấy playlist từ cơ sở dữ liệu theo ID
			var playlist = db.PlayLists
				.Include(p => p.Song_Play.Select(sp => sp.Song.Singer_Song.Select(ss => ss.Singer))) // Bao gồm ca sĩ từ mối quan hệ nhiều-nhiều
				.Include(p => p.Song_Play.Select(sp => sp.Song.Album))
				.FirstOrDefault(p => p.Ma_PlayList == id);

			// Kiểm tra nếu không tìm thấy playlist
			if (playlist == null)
			{
				return HttpNotFound();
			}

			TimeSpan totalDuration = TimeSpan.Zero;
			foreach (var songPlay in playlist.Song_Play)
			{
				totalDuration += songPlay.Song.Thoi_Luong; // Cộng thời gian của mỗi bài hát
			}

			var viewModel = new PlayListDetailViewModel
			{
				Ma_PlayList = playlist.Ma_PlayList,
				Ten_PlayList = playlist.Ten_PlayList,
				HinhAnh = playlist.HinhAnh,
				SoLuongBaiHat = playlist.Song_Play.Count, // Đếm số lượng bài hát
				ThoiLuongTongCong = totalDuration, // Gán tổng thời gian
				Songs = playlist.Song_Play.Select(sp => new SongViewModel
				{
					Ma_Bai_Hat = sp.Song.Ma_Bai_Hat,
					Ten_Bai_Hat = sp.Song.Ten_Bai_Hat,
					Thoi_Luong = sp.Song.Thoi_Luong,
					HinhAnh = sp.Song.HinhAnh,
					MP3 = sp.Song.MP3,
					Singer_Song = sp.Song.Singer_Song.ToList(), // Gán danh sách Singer_Song
					Ten_Album = sp.Song.Album?.Ten_Album,
					Ma_Album = sp.Song.Album?.Ma_Album, // Thêm Ma_Album ở đây
					Video = sp.Song.Video,
				}).ToList()
			};

			var top3Singers = playlist.Song_Play
				.Take(3) // Lấy 3 bài hát đầu tiên
				.SelectMany(sp => sp.Song.Singer_Song.Select(s => s.Singer.Ten_Ca_Si)) // Lấy tên ca sĩ
				.Distinct() // Loại bỏ tên trùng lặp
				.ToList();

			string singersDisplay = string.Join(", ", top3Singers);
			if (top3Singers.Count > 3)
			{
				singersDisplay += " and more";
			}

			ViewBag.SingersDisplay = singersDisplay;

			// Trả về view với đối tượng PlayList
			return View(viewModel);
		}

		public ActionResult SingerDetail(int id)
		{
			var singer = db.Singers
						   .Include(s => s.Songs) // Load danh sách bài hát của ca sĩ
						   .FirstOrDefault(s => s.Ma_Ca_Si == id);

			if (singer == null)
			{
				return HttpNotFound(); // Trả về lỗi 404 nếu không tìm thấy ca sĩ
			}
			var viewModel = new SingerDetailViewModel
			{
				Ma_Ca_Si = singer.Ma_Ca_Si,
				Ten_Ca_Si = singer.Ten_Ca_Si,
				HinhAnh2 = singer.HinhAnh2,
				HinhAnh = singer.HinhAnh,
				Description = singer.Description,
				Songs = singer.Singer_Song.Select(ss => new SongViewModel
				{
					Ma_Bai_Hat = ss.Song.Ma_Bai_Hat,
					Ten_Bai_Hat = ss.Song.Ten_Bai_Hat,
					Thoi_Luong = ss.Song.Thoi_Luong,
					HinhAnh = ss.Song.HinhAnh,
					MP3 = ss.Song.MP3,
					Singer_Song = ss.Song.Singer_Song.ToList(),
					Ten_Album = ss.Song.Album?.Ten_Album,
					Video = ss.Song.Video
				}).ToList()
			};

			return View(viewModel);
		}

		public ActionResult AlbumDetail(int id)
		{
			// Lấy album từ cơ sở dữ liệu và bao gồm danh sách bài hát
			var album = db.Albums
						  .Include(a => a.Songs) // Load danh sách bài hát thuộc album
						  .FirstOrDefault(a => a.Ma_Album == id);

			if (album == null)
			{
				return HttpNotFound(); // Trả về lỗi 404 nếu không tìm thấy album
			}

			// Tạo viewModel cho AlbumDetail
			var viewModel = new AlbumDetailViewModel
			{
				Ma_Album = album.Ma_Album,
				Ten_Album = album.Ten_Album,
				HinhAnh = album.HinhAnh,
				Ngay_Phat_Hanh = album.Ngay_Phat_Hanh,
				Ten_Ca_Si = album.Singer.Ten_Ca_Si, // Lấy tên ca sĩ từ quan hệ
				SoLuongBaiHat = album.Songs.Count, // Đếm số lượng bài hát
				ThoiLuongDauTien = album.Songs.FirstOrDefault()?.Thoi_Luong ?? TimeSpan.Zero,
				Songs = album.Songs.Select(s => new SongViewModel
				{
					Ma_Bai_Hat = s.Ma_Bai_Hat,
					Ten_Bai_Hat = s.Ten_Bai_Hat,
					Thoi_Luong = s.Thoi_Luong,
					HinhAnh = s.HinhAnh,
					MP3 = s.MP3,
					Singer_Song = s.Singer_Song.ToList(),
					Video = s.Video
				}).ToList()
			};

			return View(viewModel);
		}

		public ActionResult Search(string query)
		{
			var viewModel = new SearchResultsViewModel
			{
				TopResult = null,
				Songs = new List<SongViewModel>(),
				ArtistNames = new List<SingerDetailViewModel>(), // Initialize the list for artist names
				Albums = new List<AlbumDetailViewModel>(), // Initialize the list for albums
				Playlists = new List<PlayListDetailViewModel>()
			};

			if (!string.IsNullOrEmpty(query))
			{
				// Retrieve all songs that match the search query
				var matchedSongs = db.Songs
					.Where(s => s.Ten_Bai_Hat.Contains(query))
					.ToList();

				var matchedAlbums = db.Albums
					.Where(a => a.Ten_Album.Contains(query))
					.ToList();

				var matchedPlaylists = db.PlayLists
					.Where(a => a.Ten_PlayList.Contains(query))
					.ToList();

				if (matchedSongs.Any())
				{
					// Get the top result (the first matched song)
					var topResult = matchedSongs.First();

					// Set the top result in the ViewModel
					viewModel.TopResult = new SongViewModel
					{
						Ma_Bai_Hat = topResult.Ma_Bai_Hat,
						Ten_Bai_Hat = topResult.Ten_Bai_Hat,
						Thoi_Luong = topResult.Thoi_Luong,
						HinhAnh = topResult.HinhAnh,
						MP3 = topResult.MP3,
						Singer_Song = topResult.Singer_Song.ToList(),
						Video = topResult.Video
					};

					// Populate the Songs list in the ViewModel with all matched songs
					viewModel.Songs = matchedSongs.Select(song => new SongViewModel
					{
						Ma_Bai_Hat = song.Ma_Bai_Hat,
						Ten_Bai_Hat = song.Ten_Bai_Hat,
						Thoi_Luong = song.Thoi_Luong,
						HinhAnh = song.HinhAnh,
						MP3 = song.MP3,
						Singer_Song = song.Singer_Song.ToList(),
						Video = song.Video
					}).ToList();

					// Collect all unique singers from the top result and all matched songs
					var singerDetails = new HashSet<string>(); // Use HashSet of strings for names

					// Add singers from the top result
					foreach (var singer in viewModel.TopResult.Singer_Song)
					{
						singerDetails.Add(singer.Singer.Ten_Ca_Si.Trim()); // Normalize
					}

					// Add singers from all matched songs
					foreach (var song in matchedSongs)
					{
						foreach (var singer in song.Singer_Song)
						{
							singerDetails.Add(singer.Singer.Ten_Ca_Si.Trim()); // Normalize
						}
					}

					// Convert the unique singer names to a list of SingerDetailViewModel
					viewModel.ArtistNames = singerDetails.Select(name => new SingerDetailViewModel
					{
						Ma_Ca_Si = db.Singers.FirstOrDefault(s => s.Ten_Ca_Si.Trim().ToLower() == name)?.Ma_Ca_Si ?? 0,
						Ten_Ca_Si = name,
						HinhAnh = db.Singers.FirstOrDefault(s => s.Ten_Ca_Si.Trim().ToLower() == name)?.HinhAnh
					}).ToList();

					// If there are less than 6 artists, add random artists to fill up
					if (viewModel.ArtistNames.Count < 6)
					{
						// Get existing artist names
						var existingArtistNames = viewModel.ArtistNames.Select(sd => sd.Ten_Ca_Si).ToList();

						// Get all artists from the database, excluding existing ones
						var allArtists = db.Singers
							.Where(a => !existingArtistNames.Contains(a.Ten_Ca_Si.Trim().ToLower())) // Ensure we're excluding names correctly
							.OrderBy(a => Guid.NewGuid()) // Randomize
							.Take(6 - viewModel.ArtistNames.Count) // Get enough artists to make 6 total
							.Select(a => new SingerDetailViewModel
							{
								Ma_Ca_Si = a.Ma_Ca_Si,
								Ten_Ca_Si = a.Ten_Ca_Si,
								HinhAnh = a.HinhAnh
							})
							.ToList();

						// Add these random artists to the ArtistNames list
						viewModel.ArtistNames.AddRange(allArtists);
					}
				}

				viewModel.Albums = matchedAlbums.Select(album => new AlbumDetailViewModel
				{
					Ma_Album = album.Ma_Album,
					Ten_Album = album.Ten_Album,
					HinhAnh = album.HinhAnh,
					Ten_Ca_Si = album.Singer.Ten_Ca_Si,
					Ngay_Phat_Hanh = album.Ngay_Phat_Hanh,
				}).ToList();

				viewModel.Playlists = matchedPlaylists.Select(playlist => new PlayListDetailViewModel
				{
					Ma_PlayList = playlist.Ma_PlayList,
					Ten_PlayList = playlist.Ten_PlayList,
					HinhAnh = playlist.HinhAnh,
					Ten_User = playlist.Account.UserName,
				}).ToList();
			}

			// Return the view with the populated ViewModel
			return View(viewModel);
		}







		public ActionResult AdminTrangChu()
        {
            return View();
        }
    }
}