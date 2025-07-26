using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Linq;
using System.Net;
using System.Web;
using System.Web.Mvc;
using System.Web.UI.WebControls;
using MusicApp.Models;

namespace MusicApp.Controllers
{
    public class SongsController : Controller
    {
        private DAPMMainEntities db = new DAPMMainEntities();

        // GET: Songs
        public ActionResult Index()
        {
            var songs = db.Songs.Include(s => s.Album).Include(s => s.Singer);
            return View(songs.ToList());
        }

        // GET: Songs/Details/5
        public ActionResult Details(int? id)
        {
            if (id == null)
            {
                return new HttpStatusCodeResult(HttpStatusCode.BadRequest);
            }
            Song song = db.Songs.Find(id);
            if (song == null)
            {
                return HttpNotFound();
            }
            return View(song);
        }

        // GET: Songs/Create
        public ActionResult Create()
        {
            ViewBag.Ma_Album = new SelectList(db.Albums, "Ma_Album", "Ten_Album");
            ViewBag.Ma_The_Loai = new SelectList(db.Categories, "Ma_The_Loai", "Ten_The_Loai");
            ViewBag.Ma_Ca_Si = new SelectList(db.Singers, "Ma_Ca_Si", "Ten_Ca_Si");
            ViewBag.Ma_PlayList = new SelectList(db.PlayLists, "Ma_PlayList", "Ten_PlayList");
            return View();
        }

        // POST: Songs/Create
        [HttpPost]
        [ValidateAntiForgeryToken]
        public ActionResult Create([Bind(Include = "Ma_Bai_Hat,Ten_Bai_Hat,Thoi_Luong,HinhAnh,MP3,Video,Ma_Album,Ma_Ca_Si,Ma_The_Loai,Ma_PlayList")] Song song,
                           HttpPostedFileBase HinhAnh, HttpPostedFileBase Video, HttpPostedFileBase MP3, int? Ma_PlayList, int? Ma_Ca_Si)
        {
            try
            {
                // Xử lý tệp hình ảnh
                if (HinhAnh != null && HinhAnh.ContentLength > 0)
                {
                    string fileName = Guid.NewGuid() + System.IO.Path.GetExtension(HinhAnh.FileName);
                    string path = System.IO.Path.Combine(Server.MapPath("~/Images/"), fileName);
                    HinhAnh.SaveAs(path);
                    song.HinhAnh = "/Images/" + fileName;
                }
                else
                {
                    song.HinhAnh = "/Images/default-avatar.jpg";
                }

                // Xử lý tệp video
                if (Video != null && Video.ContentLength > 0)
                {
                    string videoFileName = Guid.NewGuid() + System.IO.Path.GetExtension(Video.FileName);
                    string videoPath = System.IO.Path.Combine(Server.MapPath("~/Videos/"), videoFileName);
                    Video.SaveAs(videoPath);
                    song.Video = "/Videos/" + videoFileName;
                }
                else
                {
                    song.Video = null; // Gán giá trị null nếu không có video
                }

                // Xử lý tệp MP3
                if (MP3 != null && MP3.ContentLength > 0)
                {
                    string mp3FileName = Guid.NewGuid() + System.IO.Path.GetExtension(MP3.FileName);
                    string mp3Path = System.IO.Path.Combine(Server.MapPath("~/MP3/"), mp3FileName);
                    MP3.SaveAs(mp3Path);
                    song.MP3 = "/MP3/" + mp3FileName; // Lưu đường dẫn MP3 vào cơ sở dữ liệu
                }
                else
                {
                    song.MP3 = null; // Gán giá trị null nếu không có MP3
                }

                // Lưu bài hát vào cơ sở dữ liệu
                db.Songs.Add(song);
                db.SaveChanges();

				if (Ma_PlayList.HasValue)
				{
					var songPlay = new Song_Play
					{
						Ma_PlayList = Ma_PlayList.Value,
						Ma_Bai_Hat = song.Ma_Bai_Hat
					};
					db.Song_Play.Add(songPlay); // Thêm bản ghi vào bảng Song_Play
					db.SaveChanges();
				}

				if (Ma_Ca_Si.HasValue)
				{
					var singerSong = new Singer_Song
					{
						Ma_Ca_Si = Ma_Ca_Si.Value,
						Ma_Bai_Hat = song.Ma_Bai_Hat
					};
					db.Singer_Song.Add(singerSong); // Thêm bản ghi vào bảng Song_Play
					db.SaveChanges();
				}

				return RedirectToAction("Index");
            }
            catch (Exception ex)
            {
                ModelState.AddModelError("", "Lỗi khi upload hình ảnh hoặc video: " + ex.Message);
            }

            ViewBag.Ma_Album = new SelectList(db.Albums, "Ma_Album", "Ten_Album", song.Ma_Album);
            ViewBag.Ma_Ca_Si = new SelectList(db.Singers, "Ma_Ca_Si", "Ten_Ca_Si", Ma_Ca_Si);
            ViewBag.Ma_PlayList = new SelectList(db.PlayLists, "Ma_PlayList", "Ten_PlayList",Ma_PlayList);
            return View(song);
        }



        // GET: Songs/Edit/5
        public ActionResult Edit(int? id)
        {
            if (id == null)
            {
                return new HttpStatusCodeResult(HttpStatusCode.BadRequest);
            }

			Song song = db.Songs.Include(s => s.Song_Play.Select(sp => sp.PlayList))
								.Include(s => s.Singer_Song.Select(ss => ss.Singer))
								.FirstOrDefault(s => s.Ma_Bai_Hat == id);
			if (song == null)
            {
                return HttpNotFound();
            }

            ViewBag.Ma_Album = new SelectList(db.Albums, "Ma_Album", "Ten_Album", song.Ma_Album);
			var selectedSingers = song.Singer_Song.FirstOrDefault()?.Ma_Ca_Si;
			ViewBag.Ma_Ca_Si = new SelectList(db.Singers, "Ma_Ca_Si", "Ten_Ca_Si", selectedSingers);
			var selectedPlayLists = song.Song_Play.FirstOrDefault()?.Ma_PlayList;
			ViewBag.Ma_PlayList = new SelectList(db.PlayLists, "Ma_PlayList", "Ten_PlayList", selectedPlayLists);
			return View(song);
        }

		[HttpPost] // Chỉ định rằng đây là phương thức xử lý yêu cầu POST
		[ValidateAntiForgeryToken] // Bảo mật chống tấn công CSRF
		public ActionResult Edit([Bind(Include = "Ma_Bai_Hat,Ten_Bai_Hat,Thoi_Luong,HinhAnh,MP3,Video,Ma_Album,Ma_Ca_Si,Ma_The_Loai,Ma_PlayList")] Song song,
						HttpPostedFileBase HinhAnh, HttpPostedFileBase Video, HttpPostedFileBase MP3, int? Ma_PlayList, int? Ma_Ca_Si)
		{
			try
			{
				// Tìm bài hát gốc trong cơ sở dữ liệu
				var existingSong = db.Songs.Include(s => s.Song_Play)
										   .Include(s => s.Singer_Song)
										   .FirstOrDefault(s => s.Ma_Bai_Hat == song.Ma_Bai_Hat);
				if (existingSong == null)
				{
					return HttpNotFound();
				}

				// Xử lý tệp hình ảnh, video và MP3 (nếu có)
				if (HinhAnh != null && HinhAnh.ContentLength > 0)
				{
					string fileName = Guid.NewGuid() + System.IO.Path.GetExtension(HinhAnh.FileName);
					string path = System.IO.Path.Combine(Server.MapPath("~/Images/"), fileName);
					HinhAnh.SaveAs(path);
					existingSong.HinhAnh = "/Images/" + fileName;
				}

				if (Video != null && Video.ContentLength > 0)
				{
					string videoFileName = Guid.NewGuid() + System.IO.Path.GetExtension(Video.FileName);
					string videoPath = System.IO.Path.Combine(Server.MapPath("~/Videos/"), videoFileName);
					Video.SaveAs(videoPath);
					existingSong.Video = "/Videos/" + videoFileName;
				}

				if (MP3 != null && MP3.ContentLength > 0)
				{
					string mp3FileName = Guid.NewGuid() + System.IO.Path.GetExtension(MP3.FileName);
					string mp3Path = System.IO.Path.Combine(Server.MapPath("~/MP3/"), mp3FileName);
					MP3.SaveAs(mp3Path);
					existingSong.MP3 = "/MP3/" + mp3FileName;
				}

				// Cập nhật các thuộc tính khác của bài hát
				existingSong.Ten_Bai_Hat = song.Ten_Bai_Hat;
				existingSong.Thoi_Luong = song.Thoi_Luong;
				existingSong.Ma_Album = song.Ma_Album;

				// Xử lý Playlist: Nếu playlist đã chọn khác với playlist hiện tại
				var currentSongPlay = existingSong.Song_Play.FirstOrDefault();
				if (Ma_PlayList.HasValue && (currentSongPlay == null || currentSongPlay.Ma_PlayList != Ma_PlayList))
				{
					// Xóa playlist cũ (nếu có)
					if (currentSongPlay != null)
					{
						db.Song_Play.Remove(currentSongPlay);
					}
					// Thêm bài hát vào playlist mới (nếu có chọn)
					if (Ma_PlayList.HasValue)
					{
						var newSongPlay = new Song_Play
						{
							Ma_PlayList = Ma_PlayList.Value,
							Ma_Bai_Hat = existingSong.Ma_Bai_Hat
						};
						db.Song_Play.Add(newSongPlay);
					}
				}

				// Xử lý Singer: Nếu ca sĩ đã chọn khác với ca sĩ hiện tại
				var currentSingerSong = existingSong.Singer_Song.FirstOrDefault();
				if (Ma_Ca_Si.HasValue && (currentSingerSong == null || currentSingerSong.Ma_Ca_Si != Ma_Ca_Si))
				{
					// Xóa ca sĩ cũ (nếu có)
					if (currentSingerSong != null)
					{
						db.Singer_Song.Remove(currentSingerSong);
					}
					// Thêm bài hát vào ca sĩ mới (nếu có chọn)
					if (Ma_Ca_Si.HasValue)
					{
						var newSingerSong = new Singer_Song
						{
							Ma_Ca_Si = Ma_Ca_Si.Value,
							Ma_Bai_Hat = existingSong.Ma_Bai_Hat
						};
						db.Singer_Song.Add(newSingerSong);
					}
				}

				// Lưu thay đổi
				db.Entry(existingSong).State = EntityState.Modified;
				db.SaveChanges();

				return RedirectToAction("Index");
			}
			catch (Exception ex)
			{
				ModelState.AddModelError("", "Lỗi khi cập nhật hình ảnh, video, hoặc MP3: " + ex.Message);
			}

			// Nếu có lỗi, trả về view với dữ liệu đã đổ vào SelectList
			ViewBag.Ma_Album = new SelectList(db.Albums, "Ma_Album", "Ten_Album", song.Ma_Album);
			ViewBag.Ma_Ca_Si = new SelectList(db.Singers, "Ma_Ca_Si", "Ten_Ca_Si", Ma_Ca_Si);
			ViewBag.Ma_PlayList = new SelectList(db.PlayLists, "Ma_PlayList", "Ten_PlayList", Ma_PlayList);

			return View(song);
		}


		// GET: Songs/Add
		public ActionResult Add(int? Ma_Bai_Hat)
		{
			ViewBag.Songs = new SelectList(db.Songs, "Ma_Bai_Hat", "Ten_Bai_Hat");
			ViewBag.PlayLists = new SelectList(db.PlayLists, "Ma_PlayList", "Ten_PlayList");
			return View();
		}

		// POST: Songs/Add
		[HttpPost]
		[ValidateAntiForgeryToken]
		public ActionResult Add(int Ma_Bai_Hat, int Ma_PlayList)
		{
			try
			{
				// Tìm bài hát và playlist
				var song = db.Songs.Find(Ma_Bai_Hat);
				var playlist = db.PlayLists.Find(Ma_PlayList);

				if (song == null || playlist == null)
				{
					return HttpNotFound();
				}

				// Kiểm tra xem bài hát đã có trong playlist chưa
				if (!playlist.Song_Play.Any(sp => sp.Ma_Bai_Hat == song.Ma_Bai_Hat))
				{
					// Thêm bài hát vào playlist
					var songPlay = new Song_Play
					{
						Ma_Bai_Hat = song.Ma_Bai_Hat,
						Ma_PlayList = playlist.Ma_PlayList
					};

					db.Song_Play.Add(songPlay);
					db.SaveChanges();
				}

				return RedirectToAction("Index"); // Điều hướng đến trang index hoặc trang bạn muốn sau khi thêm
			}
			catch (Exception ex)
			{
				ModelState.AddModelError("", "Lỗi khi thêm bài hát vào playlist: " + ex.Message);
			}

			ViewBag.Songs = new SelectList(db.Songs, "Ma_Bai_Hat", "Ten_Bai_Hat");
			ViewBag.PlayLists = new SelectList(db.PlayLists, "Ma_PlayList", "Ten_PlayList");
			return View();
		}

		// GET: Songs/AddSinger
		public ActionResult AddSinger(int? Ma_Bai_Hat)
		{
			ViewBag.Songs = new SelectList(db.Songs, "Ma_Bai_Hat", "Ten_Bai_Hat");
			ViewBag.Singers = new SelectList(db.Singers, "Ma_Ca_Si", "Ten_Ca_Si");
			return View();
		}

		// POST: Songs/AddSinger
		[HttpPost]
		[ValidateAntiForgeryToken]
		public ActionResult AddSinger(int Ma_Bai_Hat, int Ma_Ca_Si)
		{
			try
			{
				// Tìm bài hát và playlist
				var song = db.Songs.Find(Ma_Bai_Hat);
				var singer = db.Singers.Find(Ma_Ca_Si);

				if (song == null || singer == null)
				{
					return HttpNotFound();
				}

				// Kiểm tra xem bài hát đã có trong playlist chưa
				if (!singer.Singer_Song.Any(ss => ss.Ma_Bai_Hat == song.Ma_Bai_Hat))
				{
					// Thêm bài hát vào playlist
					var singerSong = new Singer_Song
					{
						Ma_Bai_Hat = song.Ma_Bai_Hat,
						Ma_Ca_Si = singer.Ma_Ca_Si
					};

					db.Singer_Song.Add(singerSong);
					db.SaveChanges();
				}

				return RedirectToAction("Index"); // Điều hướng đến trang index hoặc trang bạn muốn sau khi thêm
			}
			catch (Exception ex)
			{
				ModelState.AddModelError("", "Lỗi khi thêm bài hát vào playlist: " + ex.Message);
			}

			ViewBag.Songs = new SelectList(db.Songs, "Ma_Bai_Hat", "Ten_Bai_Hat");
			ViewBag.PlayLists = new SelectList(db.Singers, "Ma_Ca_Si", "Ten_Ca_Si");
			return View();
		}

		// GET: Songs/RemoveFromPlaylist
		public ActionResult RemoveFromPlaylist(int songId, int playlistId)
		{
			// Tìm bài hát trong playlist
			var songPlay = db.Song_Play.FirstOrDefault(sp => sp.Ma_Bai_Hat == songId && sp.Ma_PlayList == playlistId);

			if (songPlay == null)
			{
				return HttpNotFound("Bài hát không nằm trong playlist này");
			}

			// Xóa bài hát khỏi playlist
			db.Song_Play.Remove(songPlay);
			db.SaveChanges();

			return RedirectToAction("Index");
		}

		public ActionResult RemoveSingerFromSong(int songId, int singerId)
		{
			// Tìm bài hát trong playlist
			var singerSong = db.Singer_Song.FirstOrDefault(ss => ss.Ma_Bai_Hat == songId && ss.Ma_Ca_Si == singerId);

			if (singerSong == null)
			{
				return HttpNotFound("Ca sĩ không nằm trong song này");
			}

			// Xóa bài hát khỏi playlist
			db.Singer_Song.Remove(singerSong);
			db.SaveChanges();

			return RedirectToAction("Index");
		}


		// GET: Songs/Delete/5
		public ActionResult Delete(int? id)
        {
            if (id == null)
            {
                return new HttpStatusCodeResult(HttpStatusCode.BadRequest);
            }
            Song song = db.Songs.Find(id);
            if (song == null)
            {
                return HttpNotFound();
            }
            return View(song);
        }

		// POST: Songs/Delete/5
		[HttpPost, ActionName("Delete")]
		[ValidateAntiForgeryToken]
		public ActionResult DeleteConfirmed(int id)
		{
			Song song = db.Songs.Find(id);
			if (song == null)
			{
				return HttpNotFound();
			}

            // Xóa các tham chiếu đến bài hát trong bảng Song_Play
            var songPlayEntries = db.Song_Play.Where(sp => sp.Ma_Bai_Hat == song.Ma_Bai_Hat).ToList();
            db.Song_Play.RemoveRange(songPlayEntries); // Loại bỏ tất cả các bản ghi tham chiếu

			var singerSongEntries = db.Singer_Song.Where(ss => ss.Ma_Bai_Hat == song.Ma_Bai_Hat).ToList();
			db.Singer_Song.RemoveRange(singerSongEntries); // Loại bỏ tất cả các bản ghi tham chiếu


			// Xóa bài hát
			db.Songs.Remove(song);
			db.SaveChanges();

			return RedirectToAction("Index");
		}



		protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                db.Dispose();
            }
            base.Dispose(disposing);
        }
    }
}
