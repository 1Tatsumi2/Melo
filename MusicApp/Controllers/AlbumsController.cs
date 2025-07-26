using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.IO;
using System.Linq;
using System.Net;
using System.Reflection;
using System.Web;
using System.Web.Mvc;
using MusicApp.Models;

namespace MusicApp.Controllers
{
    public class AlbumsController : Controller
    {
        private DAPMMainEntities db = new DAPMMainEntities();

        // GET: Albums
        public ActionResult Index()
        {
            var albums = db.Albums.Include(a => a.Singer);
            return View(albums.ToList());
        }

        // GET: Albums/Details/5
        public ActionResult Details(int? id)
        {
            if (id == null)
            {
                return new HttpStatusCodeResult(HttpStatusCode.BadRequest);
            }
            Album album = db.Albums.Find(id);
            if (album == null)
            {
                return HttpNotFound();
            }
            return View(album);
        }

        // GET: Albums/Create
        public ActionResult Create()
        {
            ViewBag.Ma_Ca_Si = new SelectList(db.Singers, "Ma_Ca_Si", "Ten_Ca_Si");
            return View();
        }

        // POST: Albums/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to, for 
        // more details see https://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public ActionResult Create([Bind(Include = "Ma_Album,Ten_Album,HinhAnh,Ma_Ca_Si,Ngay_Phat_Hanh")] Album album, HttpPostedFileBase HinhAnh)
        {
			if (ModelState.IsValid)
			{
				var existingAlbum = db.Albums.FirstOrDefault(a => a.Ten_Album == album.Ten_Album);
				if (existingAlbum != null)
				{
					ModelState.AddModelError("Ten_Album", "Tên album với tên này đã tồn tại.");
					return View(album);
				}

				// Kiểm tra nếu có file hình ảnh được upload
				if (HinhAnh != null && HinhAnh.ContentLength > 0)
				{
					// Tạo tên file duy nhất để tránh trùng lặp
					string fileName = Path.GetFileName(HinhAnh.FileName);
					string uniqueFileName = $"{Guid.NewGuid()}_{fileName}"; // Đặt tên file duy nhất bằng cách thêm GUID
					string path = Path.Combine(Server.MapPath("~/Images/"), uniqueFileName);

					// Lưu file vào thư mục Images
					HinhAnh.SaveAs(path);

					// Lưu đường dẫn tương đối của hình ảnh vào thuộc tính HinhAnh
					album.HinhAnh = "/Images/" + uniqueFileName;
				}
				else
				{
					album.HinhAnh = "/Images/default-avatar.jpg"; // Gán ảnh mặc định nếu không có ảnh được upload
				}

				db.Albums.Add(album);
				db.SaveChanges();

				ViewBag.Ma_Ca_Si = new SelectList(db.Singers, "Ma_Ca_Si", "Ten_Ca_Si", album.Ma_Ca_Si);
				return RedirectToAction("Index");
			}

			// Nếu có lỗi, trả về form tạo với thông tin nhập vào
			return View(album);
		}

        // GET: Albums/Edit/5
        public ActionResult Edit(int? id)
        {
            if (id == null)
            {
                return new HttpStatusCodeResult(HttpStatusCode.BadRequest);
            }
            Album album = db.Albums.Find(id);
            if (album == null)
            {
                return HttpNotFound();
            }
            ViewBag.Ma_Ca_Si = new SelectList(db.Singers, "Ma_Ca_Si", "Ten_Ca_Si", album.Ma_Ca_Si);
            return View(album);
        }

		// POST: Albums/Edit/5
		// To protect from overposting attacks, enable the specific properties you want to bind to, for 
		// more details see https://go.microsoft.com/fwlink/?LinkId=317598.
		[HttpPost]
		[ValidateAntiForgeryToken]
		public ActionResult Edit([Bind(Include = "Ma_Album,Ten_Album,HinhAnh,Ma_Ca_Si,Ngay_Phat_Hanh")] Album album, HttpPostedFileBase HinhAnh)
		{
			if (ModelState.IsValid)
			{
				var existingAlbum = db.Albums.Find(album.Ma_Album);

				if (existingAlbum == null)
				{
					return HttpNotFound();
				}

				// Kiểm tra nếu tên album bị trùng với các album khác (ngoại trừ album hiện tại)
				var duplicateAlbum = db.Albums.FirstOrDefault(a => a.Ten_Album == album.Ten_Album && a.Ma_Album != album.Ma_Album);
				if (duplicateAlbum != null)
				{
					ModelState.AddModelError("Ten_Album", "Tên album này đã tồn tại.");
					return View(album);
				}

				// Kiểm tra nếu có file hình ảnh mới được upload
				if (HinhAnh != null && HinhAnh.ContentLength > 0)
				{
					// Tạo tên file duy nhất để tránh trùng lặp
					string fileName = Path.GetFileName(HinhAnh.FileName);
					string uniqueFileName = $"{Guid.NewGuid()}_{fileName}"; // Đặt tên file duy nhất bằng cách thêm GUID
					string path = Path.Combine(Server.MapPath("~/Images/"), uniqueFileName);

					// Lưu file vào thư mục Images
					HinhAnh.SaveAs(path);

					// Lưu đường dẫn tương đối của hình ảnh vào thuộc tính HinhAnh
					existingAlbum.HinhAnh = "/Images/" + uniqueFileName;
				}
				else
				{
					// Nếu không có hình ảnh mới, giữ lại hình ảnh cũ
					album.HinhAnh = existingAlbum.HinhAnh;
				}

				// Cập nhật các thuộc tính khác của album
				existingAlbum.Ten_Album = album.Ten_Album;
				existingAlbum.Ma_Ca_Si = album.Ma_Ca_Si;
				existingAlbum.Ngay_Phat_Hanh = album.Ngay_Phat_Hanh;

				db.Entry(existingAlbum).State = EntityState.Modified;
				db.SaveChanges();

				ViewBag.Ma_Ca_Si = new SelectList(db.Singers, "Ma_Ca_Si", "Ten_Ca_Si", existingAlbum.Ma_Ca_Si);
				return RedirectToAction("Index");
			}

			// Nếu có lỗi, trả về form chỉnh sửa với thông tin đã nhập
			return View(album);
		}


		// GET: Albums/Delete/5
		public ActionResult Delete(int? id)
        {
            if (id == null)
            {
                return new HttpStatusCodeResult(HttpStatusCode.BadRequest);
            }
            Album album = db.Albums.Find(id);
            if (album == null)
            {
                return HttpNotFound();
            }
            return View(album);
        }

        // POST: Albums/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public ActionResult DeleteConfirmed(int id)
        {
            Album album = db.Albums.Find(id);
            db.Albums.Remove(album);
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
