using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.IO;
using System.Linq;
using System.Net;
using System.Security.Principal;
using System.Web;
using System.Web.Mvc;
using MusicApp.Models;

namespace MusicApp.Controllers
{
    public class SingersController : Controller
    {
        private DAPMMainEntities db = new DAPMMainEntities();

        // GET: Singers
        public ActionResult Index()
        {
            return View(db.Singers.ToList());
        }

        // GET: Singers/Details/5
        public ActionResult Details(int? id)
        {
            if (id == null)
            {
                return new HttpStatusCodeResult(HttpStatusCode.BadRequest);
            }
            Singer singer = db.Singers.Find(id);
            if (singer == null)
            {
                return HttpNotFound();
            }
            return View(singer);
        }

        // GET: Singers/Create
        public ActionResult Create()
        {
            return View();
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public ActionResult Create(Singer singer, HttpPostedFileBase HinhAnh, HttpPostedFileBase HinhAnh2)
        {
            if (ModelState.IsValid)
            {
                var existingSinger = db.Singers.FirstOrDefault(s => s.Ten_Ca_Si == singer.Ten_Ca_Si);
                if (existingSinger != null)
                {
                    ModelState.AddModelError("Ten_Ca_Si", "Ca sĩ với tên này đã tồn tại.");
                    return View(singer);
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
                    singer.HinhAnh = "/Images/" + uniqueFileName;
                }
                else
                {
                    singer.HinhAnh = "/Images/default-avatar.jpg"; // Gán ảnh mặc định nếu không có ảnh được upload
                }

				if (HinhAnh2 != null && HinhAnh2.ContentLength > 0)
				{
					// Tạo tên file duy nhất để tránh trùng lặp
					string fileName = Path.GetFileName(HinhAnh2.FileName);
					string uniqueFileName = $"{Guid.NewGuid()}_{fileName}"; // Đặt tên file duy nhất bằng cách thêm GUID
					string path = Path.Combine(Server.MapPath("~/Images/"), uniqueFileName);

					// Lưu file vào thư mục Images
					HinhAnh.SaveAs(path);

					// Lưu đường dẫn tương đối của hình ảnh vào thuộc tính HinhAnh
					singer.HinhAnh2 = "/Images/" + uniqueFileName;
				}
				else
				{
					singer.HinhAnh2 = "/Images/default-avatar.jpg"; // Gán ảnh mặc định nếu không có ảnh được upload
				}


				// Thêm ca sĩ vào cơ sở dữ liệu
				db.Singers.Add(singer);
                db.SaveChanges();

                // Chuyển hướng về trang danh sách sau khi tạo thành công
                return RedirectToAction("Index");
            }

            // Nếu có lỗi, trả về form tạo với thông tin nhập vào
            return View(singer);
        }



        // GET: Singers/Edit/5
        public ActionResult Edit(int? id)
        {
            if (id == null)
            {
                return new HttpStatusCodeResult(HttpStatusCode.BadRequest);
            }
            Singer singer = db.Singers.Find(id);
            if (singer == null)
            {
                return HttpNotFound();
            }
            return View(singer);
        }

		[HttpPost]
		[ValidateAntiForgeryToken]
		public ActionResult Edit(Singer singer, HttpPostedFileBase HinhAnh, HttpPostedFileBase HinhAnh2)
		{
			if (ModelState.IsValid)
			{
				// Kiểm tra trùng tên ca sĩ (trừ ca sĩ hiện tại)
				var existingSinger = db.Singers.FirstOrDefault(s => s.Ten_Ca_Si == singer.Ten_Ca_Si && s.Ma_Ca_Si != singer.Ma_Ca_Si);
				if (existingSinger != null)
				{
					ModelState.AddModelError("Ten_Ca_Si", "Ca sĩ với tên này đã tồn tại.");
					return View(singer);
				}

				// Tìm thông tin ca sĩ hiện tại trong database để lấy đường dẫn hình ảnh cũ nếu không thay đổi
				var currentSinger = db.Singers.AsNoTracking().FirstOrDefault(s => s.Ma_Ca_Si == singer.Ma_Ca_Si);
				if (currentSinger == null)
				{
					return HttpNotFound(); // Trả về lỗi nếu không tìm thấy ca sĩ
				}

				// Xử lý ảnh nếu có upload ảnh mới
				if (HinhAnh != null && HinhAnh.ContentLength > 0)
				{
					// Tạo tên file duy nhất để tránh trùng lặp
					string fileName = Path.GetFileName(HinhAnh.FileName);
					string uniqueFileName = $"{Guid.NewGuid()}_{fileName}"; // Tạo tên file duy nhất bằng cách thêm GUID
					string path = Path.Combine(Server.MapPath("~/Images/"), uniqueFileName);

					// Lưu file vào thư mục Images
					HinhAnh.SaveAs(path);

					// Lưu đường dẫn tương đối của hình ảnh vào thuộc tính HinhAnh
					singer.HinhAnh = "/Images/" + uniqueFileName;
				}
				else
				{
					// Nếu không thay đổi ảnh, giữ nguyên ảnh cũ
					singer.HinhAnh = currentSinger.HinhAnh;
				}

				if (HinhAnh2 != null && HinhAnh2.ContentLength > 0)
				{
					// Tạo tên file duy nhất để tránh trùng lặp
					string fileName = Path.GetFileName(HinhAnh2.FileName);
					string uniqueFileName = $"{Guid.NewGuid()}_{fileName}"; // Tạo tên file duy nhất bằng cách thêm GUID
					string path = Path.Combine(Server.MapPath("~/Images/"), uniqueFileName);

					// Lưu file vào thư mục Images
					HinhAnh2.SaveAs(path);

					// Lưu đường dẫn tương đối của hình ảnh vào thuộc tính HinhAnh
					singer.HinhAnh2 = "/Images/" + uniqueFileName;
				}
				else
				{
					// Nếu không thay đổi ảnh, giữ nguyên ảnh cũ
					singer.HinhAnh2 = currentSinger.HinhAnh2;
				}

				// Cập nhật thông tin ca sĩ
				db.Entry(singer).State = EntityState.Modified;
				db.SaveChanges();

				// Chuyển hướng về trang danh sách sau khi cập nhật thành công
				return RedirectToAction("Index");
			}

			// Nếu có lỗi, trả về form edit với thông tin nhập vào
			return View(singer);
		}


		// GET: Singers/Delete/5
		public ActionResult Delete(int? id)
        {
            if (id == null)
            {
                return new HttpStatusCodeResult(HttpStatusCode.BadRequest);
            }
            Singer singer = db.Singers.Find(id);
            if (singer == null)
            {
                return HttpNotFound();
            }
            return View(singer);
        }

        // POST: Singers/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public ActionResult DeleteConfirmed(int id)
        {
            Singer singer = db.Singers.Find(id);
            db.Singers.Remove(singer);
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
