using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.IO;
using System.Linq;
using System.Net;
using System.Web;
using System.Web.Mvc;
using MusicApp.Models;

namespace MusicApp.Controllers
{
    public class PlayListsController : Controller
    {
        private DAPMMainEntities db = new DAPMMainEntities();

        // GET: PlayLists
        public ActionResult Index()
        {
            var playLists = db.PlayLists.Include(p => p.Account);
            return View(playLists.ToList());
        }

        // GET: PlayLists/Details/5
        public ActionResult Details(int? id)
        {
            if (id == null)
            {
                return new HttpStatusCodeResult(HttpStatusCode.BadRequest);
            }
            PlayList playList = db.PlayLists.Find(id);
            if (playList == null)
            {
                return HttpNotFound();
            }
            return View(playList);
        }

        // GET: PlayLists/Create
        public ActionResult Create()
        {
            ViewBag.Ma_User = new SelectList(db.Accounts, "Ma_User", "TaiKhoan");
			ViewBag.Ma_The_Loai = new SelectList(db.Categories, "Ma_The_Loai", "Ten_The_Loai")
					   .Prepend(new SelectListItem { Value = "", Text = "Không có thể loại" });
			return View();
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public ActionResult Create([Bind(Include = "Ma_PlayList,Ten_PlayList,Ma_User,Ma_The_Loai")] PlayList playList, HttpPostedFileBase HinhAnh)
        {
            try
            {
                // Kiểm tra tệp hình ảnh có được upload không
                if (HinhAnh != null && HinhAnh.ContentLength > 0)
                {
                    string fileName = Guid.NewGuid() + System.IO.Path.GetExtension(HinhAnh.FileName);
                    string path = System.IO.Path.Combine(Server.MapPath("~/Images/"), fileName);

                    // Lưu tệp hình ảnh vào thư mục
                    HinhAnh.SaveAs(path);

                    // Lưu đường dẫn ảnh vào cơ sở dữ liệu
                    playList.HinhAnh = "/Images/" + fileName;
                }
                else
                {
                    // Nếu không có tệp, dùng ảnh mặc định
                    playList.HinhAnh = "/Images/default-avatar.jpg";
                }

                // Lưu playlist vào cơ sở dữ liệu
                db.PlayLists.Add(playList);
                db.SaveChanges();
                return RedirectToAction("Index");
            }
            catch (Exception ex)
            {
                // Log lỗi để debug
                ModelState.AddModelError("", "Lỗi khi upload ảnh: " + ex.Message);
            }

            ViewBag.Ma_User = new SelectList(db.Accounts, "Ma_User", "TaiKhoan", playList.Ma_User);
			ViewBag.Ma_The_Loai = new SelectList(db.Categories, "Ma_The_Loai", "Ten_The_Loai", playList.Ma_The_Loai);
			return View(playList);
        }



        // GET: PlayLists/Edit/5
        // GET: PlayLists/Edit/5
        public ActionResult Edit(int? id)
        {
            if (id == null)
            {
                return new HttpStatusCodeResult(HttpStatusCode.BadRequest);
            }

            PlayList playList = db.PlayLists.Find(id);
            if (playList == null)
            {
                return HttpNotFound();
            }

            ViewBag.Ma_User = new SelectList(db.Accounts, "Ma_User", "TaiKhoan", playList.Ma_User);
			ViewBag.Ma_The_Loai = new SelectList(db.Categories, "Ma_The_Loai", "Ten_The_Loai", playList.Ma_The_Loai)
					   .Prepend(new SelectListItem { Value = "", Text = "Không có thể loại" });


			return View(playList);
        }

		[HttpPost]
		[ValidateAntiForgeryToken]
		public ActionResult Edit(int id, [Bind(Include = "Ma_PlayList,Ten_PlayList,Ma_User,Ma_The_Loai,HinhAnh")] PlayList updatedPlayList, HttpPostedFileBase HinhAnh)
		{
			try
			{
				// Tìm playlist hiện tại
				var existingPlayList = db.PlayLists.Find(id);
				if (existingPlayList == null)
				{
					return HttpNotFound();
				}

				// Xử lý hình ảnh mới nếu có
				if (HinhAnh != null && HinhAnh.ContentLength > 0)
				{
					// Tạo tên tệp mới để tránh trùng lặp, sử dụng Guid
					string fileName = Guid.NewGuid() + Path.GetExtension(HinhAnh.FileName);
					string path = Path.Combine(Server.MapPath("~/Images/"), fileName);

					// Lưu tệp ảnh mới
					HinhAnh.SaveAs(path);

					// Xóa tệp ảnh cũ nếu không phải ảnh mặc định
					if (existingPlayList.HinhAnh != "/Images/default-avatar.jpg")
					{
						string oldImagePath = Server.MapPath(existingPlayList.HinhAnh);
						if (System.IO.File.Exists(oldImagePath))
						{
							System.IO.File.Delete(oldImagePath);
						}
					}

					// Cập nhật đường dẫn ảnh mới
					existingPlayList.HinhAnh = "/Images/" + fileName;
				}
				else
				{
					// Nếu không có hình ảnh mới, giữ lại hình ảnh cũ
					updatedPlayList.HinhAnh = existingPlayList.HinhAnh;
				}

				// Cập nhật các thông tin khác
				existingPlayList.Ten_PlayList = updatedPlayList.Ten_PlayList;
				existingPlayList.Ma_User = updatedPlayList.Ma_User;
				existingPlayList.Ma_The_Loai = updatedPlayList.Ma_The_Loai;


				// Lưu thay đổi vào cơ sở dữ liệu
				db.Entry(existingPlayList).State = EntityState.Modified;
				db.SaveChanges();

				ViewBag.Notification = "Cập nhật thành công!";
				return RedirectToAction("Index");
			}
			catch (Exception ex)
			{
				// Xử lý lỗi và hiển thị thông báo lỗi
				ModelState.AddModelError("", "Lỗi khi cập nhật: " + ex.Message);
			}

			// Nếu có lỗi, trả về form edit cùng dữ liệu đã nhập
			ViewBag.Ma_User = new SelectList(db.Accounts, "Ma_User", "TaiKhoan", updatedPlayList.Ma_User);
			ViewBag.Ma_The_Loai = new SelectList(db.Categories, "Ma_The_Loai", "Ten_The_Loai", updatedPlayList.Ma_The_Loai);
			return View(updatedPlayList);
		}



		// GET: PlayLists/Delete/5
		public ActionResult Delete(int? id)
        {
            if (id == null)
            {
                return new HttpStatusCodeResult(HttpStatusCode.BadRequest);
            }
            PlayList playList = db.PlayLists.Find(id);
            if (playList == null)
            {
                return HttpNotFound();
            }
            return View(playList);
        }

        // POST: PlayLists/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public ActionResult DeleteConfirmed(int id)
        {
            PlayList playList = db.PlayLists.Find(id);
            db.PlayLists.Remove(playList);
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
