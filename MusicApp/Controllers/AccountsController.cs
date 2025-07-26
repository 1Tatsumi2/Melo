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
    public class AccountsController : Controller
    {
        private DAPMMainEntities db = new DAPMMainEntities();

        // GET: Accounts
        public ActionResult Index()
        {
            return View(db.Accounts.ToList());
        }

        // GET: Accounts/Details/5
        public ActionResult Details(int? id)
        {
            if (id == null)
            {
                return new HttpStatusCodeResult(HttpStatusCode.BadRequest);
            }
            Account account = db.Accounts.Find(id);
            if (account == null)
            {
                return HttpNotFound();
            }
            return View(account);
        }

        public ActionResult Create()
        {
            return View();
        }

        [HttpPost]
        public ActionResult Create(Account account, HttpPostedFileBase HinhAnh)
        {
            try
            {

                if (db.Accounts.Any(x => x.TaiKhoan == account.TaiKhoan))
                {
                    ViewBag.Notification = "Tài khoản đã tồn tại";
                    return View(account);
                }

                // Xử lý hình ảnh
                if (HinhAnh != null && HinhAnh.ContentLength > 0)
                {
                    string fileName = Path.GetFileName(HinhAnh.FileName);
                    string path = Path.Combine(Server.MapPath("~/Images/"), fileName);
                    HinhAnh.SaveAs(path);
                    account.HinhAnh = fileName;
                }
                else
                {
                    account.HinhAnh = "default-avatar.jpg"; // Gán hình ảnh mặc định nếu không chọn ảnh
                }

                account.Role = 1; // Đặt giá trị mặc định cho Role là "User"

                db.Accounts.Add(account);
                db.SaveChanges();

                Session["Ma_User"] = account.Ma_User.ToString();
                Session["TaiKhoan"] = account.TaiKhoan.ToString();
                Session["UserName"] = account.UserName.ToString();
                Session["HinhAnh"] = account.HinhAnh.ToString();
                Session["Role"] = account.Role.ToString();
                Session["Email"] = account.Email.ToString();


                return RedirectToAction("Index", "Accounts");
            }
            catch (System.Data.Entity.Validation.DbEntityValidationException ex)
            {
                // Xử lý lỗi validation và ghi lại thông tin
                foreach (var validationErrors in ex.EntityValidationErrors)
                {
                    foreach (var validationError in validationErrors.ValidationErrors)
                    {
                        System.Diagnostics.Debug.WriteLine($"Property: {validationError.PropertyName} Error: {validationError.ErrorMessage}");
                    }
                }
                ViewBag.Notification = "Đăng ký thất bại, vui lòng kiểm tra lại thông tin!";
                return View(account);
            }

        }
        // GET: Accounts/Edit/5
        public ActionResult Edit(int? id)
        {
            if (id == null)
            {
                return new HttpStatusCodeResult(HttpStatusCode.BadRequest);
            }
            Account account = db.Accounts.Find(id);
            if (account == null)
            {
                return HttpNotFound();
            }
            return View(account);
        }

		// POST: Accounts/Edit/5
		// POST: Accounts/Edit/5
		[HttpPost]
		[ValidateAntiForgeryToken]
		public ActionResult Edit(int id, Account updatedAccount, HttpPostedFileBase HinhAnh)
		{
			try
			{
				// Tìm tài khoản hiện tại
				var existingAccount = db.Accounts.Find(id);
				if (existingAccount == null)
				{
					return HttpNotFound();
				}

				// Tự động gán NhapLaiMatKhau bằng với MatKhau
				updatedAccount.NhapLaiMatKhau = updatedAccount.MatKhau;

				// Kiểm tra trùng tên tài khoản
				if (db.Accounts.Any(x => x.TaiKhoan == updatedAccount.TaiKhoan && x.Ma_User != id))
				{
					ViewBag.Notification = "Tên tài khoản đã được sử dụng!";
					return View(updatedAccount);
				}

				// Xử lý hình ảnh nếu có upload ảnh mới
				if (HinhAnh != null && HinhAnh.ContentLength > 0)
				{
					// Tạo tên file duy nhất để tránh trùng lặp
					string fileName = Path.GetFileName(HinhAnh.FileName);
					string uniqueFileName = $"{Guid.NewGuid()}_{fileName}"; // Tạo tên file duy nhất bằng cách thêm GUID
					string path = Path.Combine(Server.MapPath("~/Images/"), uniqueFileName);
					HinhAnh.SaveAs(path);

					// Cập nhật đường dẫn hình ảnh mới
					existingAccount.HinhAnh = uniqueFileName;
				}
				else
				{
					// Nếu không thay đổi ảnh, giữ nguyên ảnh cũ
					updatedAccount.HinhAnh = existingAccount.HinhAnh;
				}

				// Cập nhật các thông tin khác
				existingAccount.TaiKhoan = updatedAccount.TaiKhoan;
				existingAccount.MatKhau = updatedAccount.MatKhau;
				existingAccount.UserName = updatedAccount.UserName;
				existingAccount.Email = updatedAccount.Email;
				existingAccount.Role = updatedAccount.Role;
				existingAccount.NhapLaiMatKhau = updatedAccount.NhapLaiMatKhau;

				// Lưu thay đổi vào cơ sở dữ liệu
				db.Entry(existingAccount).State = EntityState.Modified;
				db.SaveChanges();

				// Cập nhật session nếu cần
				if (Session["Ma_User"] != null && int.TryParse(Session["Ma_User"].ToString(), out int maUser))
				{
					if (maUser == id)
					{
						Session["TaiKhoan"] = existingAccount.TaiKhoan;
						Session["UserName"] = existingAccount.UserName;
						Session["HinhAnh"] = existingAccount.HinhAnh;
						Session["Role"] = existingAccount.Role.ToString();
						Session["Email"] = existingAccount.Email;
					}
				}

				ViewBag.Notification = "Cập nhật tài khoản thành công!";
				return RedirectToAction("Index", "Accounts");
			}
			catch (System.Data.Entity.Validation.DbEntityValidationException ex)
			{
				foreach (var validationErrors in ex.EntityValidationErrors)
				{
					foreach (var validationError in validationErrors.ValidationErrors)
					{
						System.Diagnostics.Debug.WriteLine($"Property: {validationError.PropertyName} Error: {validationError.ErrorMessage}");
					}
				}
				ViewBag.Notification = "Cập nhật thất bại, vui lòng kiểm tra lại thông tin!";
				return View(updatedAccount);
			}
		}


		// GET: Accounts/Delete/5
		public ActionResult Delete(int? id)
        {
            if (id == null)
            {
                return new HttpStatusCodeResult(HttpStatusCode.BadRequest);
            }
            Account account = db.Accounts.Find(id);
            if (account == null)
            {
                return HttpNotFound();
            }
            return View(account);
        }

        // POST: Accounts/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public ActionResult DeleteConfirmed(int id)
        {
            Account account = db.Accounts.Find(id);
            db.Accounts.Remove(account);
            db.SaveChanges();
            return RedirectToAction("Index");
        }

		public ActionResult Profile(int? id)
		{
			if (id == null)
			{
				// Fallback to session if id is not provided in the URL
				id = int.TryParse(Session["Ma_User"]?.ToString(), out int userId) ? userId : (int?)null;
			}

			if (id == null)
			{
				// Redirect to login if there's no valid user ID
				return RedirectToAction("Login", "SignInUp");
			}

			// Retrieve user profile details from the database using the ID
			var user = db.Accounts.Find(id);
			if (user == null)
			{
				return HttpNotFound();
			}

			var topArtists = db.Singers
	           .OrderBy(s => s.Ten_Ca_Si) // Sắp xếp theo tên hoặc điều kiện khác tùy ý
	           .Take(6) // Giới hạn 6 ca sĩ
	           .ToList();

			// Tạo ViewModel để truyền dữ liệu
			var profileViewModel = new ProfileViewModel
			{
				User = user, // Thông tin người dùng
				TopArtists = topArtists // Danh sách ca sĩ
			};

			return View(profileViewModel);
		}

		// GET: Accounts/EditProfile
		public ActionResult EditProfile(int? id)
		{
			if (id == null)
			{
				// Nếu không có ID, lấy ID từ session
				id = int.TryParse(Session["Ma_User"]?.ToString(), out int userId) ? userId : (int?)null;
			}

			// Nếu không có ID, chuyển hướng tới trang đăng nhập
			if (id == null)
			{
				return RedirectToAction("Login", "SignInUp");
			}

			// Lấy thông tin người dùng từ cơ sở dữ liệu theo ID
			var account = db.Accounts.Find(id);
			if (account == null)
			{
				return HttpNotFound(); // Nếu không tìm thấy người dùng, trả về lỗi 404
			}

			return View(account); // Trả về view với dữ liệu người dùng
		}



		// POST: Accounts/EditProfile
		[HttpPost]
		[ValidateAntiForgeryToken]
		public ActionResult EditProfile(int? id, Account updatedAccount, HttpPostedFileBase HinhAnh)
		{
			try
			{
				// Kiểm tra xem id có hợp lệ không
				if (id == null)
				{
					id = int.TryParse(Session["Ma_User"]?.ToString(), out int userId) ? userId : (int?)null;
				}

				// Lấy tài khoản hiện tại từ cơ sở dữ liệu
				var existingAccount = db.Accounts.Find(id);
				if (existingAccount == null)
				{
					return HttpNotFound(); // Nếu không tìm thấy tài khoản, trả về lỗi 404
				}

				// Tự động gán NhapLaiMatKhau bằng với MatKhau
				updatedAccount.NhapLaiMatKhau = updatedAccount.MatKhau;

				// Kiểm tra trùng tên tài khoản
				if (db.Accounts.Any(x => x.TaiKhoan == updatedAccount.TaiKhoan && x.Ma_User != id))
				{
					ViewBag.Notification = "Tên tài khoản đã được sử dụng!";
					return View(updatedAccount); // Nếu trùng tên tài khoản, trả về thông báo lỗi
				}

				// Xử lý hình ảnh nếu có upload ảnh mới
				if (HinhAnh != null && HinhAnh.ContentLength > 0)
				{
					// Tạo tên file duy nhất để tránh trùng lặp
					string fileName = Path.GetFileName(HinhAnh.FileName);
					string uniqueFileName = $"{Guid.NewGuid()}_{fileName}"; // Tạo tên file duy nhất bằng cách thêm GUID
					string path = Path.Combine(Server.MapPath("~/Images/"), uniqueFileName);
					HinhAnh.SaveAs(path);

					// Cập nhật đường dẫn hình ảnh mới
					existingAccount.HinhAnh = uniqueFileName;
				}
				else
				{
					// Nếu không thay đổi ảnh, giữ nguyên ảnh cũ
					updatedAccount.HinhAnh = existingAccount.HinhAnh;
				}

				// Cập nhật các thông tin khác
				existingAccount.TaiKhoan = updatedAccount.TaiKhoan;
				existingAccount.MatKhau = updatedAccount.MatKhau;
				existingAccount.UserName = updatedAccount.UserName;
				existingAccount.Email = updatedAccount.Email;
				existingAccount.Role = updatedAccount.Role;
				existingAccount.NhapLaiMatKhau = updatedAccount.NhapLaiMatKhau;

				// Lưu thay đổi vào cơ sở dữ liệu
				db.Entry(existingAccount).State = EntityState.Modified;
				db.SaveChanges();

				// Cập nhật session nếu cần
				if (Session["Ma_User"] != null && int.TryParse(Session["Ma_User"].ToString(), out int maUser))
				{
					if (maUser == id)
					{
						Session["TaiKhoan"] = existingAccount.TaiKhoan;
						Session["UserName"] = existingAccount.UserName;
						Session["HinhAnh"] = existingAccount.HinhAnh;
						Session["Role"] = existingAccount.Role.ToString();
						Session["Email"] = existingAccount.Email;
					}
				}

				// Thông báo thành công và chuyển hướng về trang profile
				ViewBag.Notification = "Cập nhật tài khoản thành công!";
				return RedirectToAction("Profile", new { id = existingAccount.Ma_User });
			}
			catch (System.Data.Entity.Validation.DbEntityValidationException ex)
			{
				// Nếu có lỗi khi lưu, in ra thông báo lỗi chi tiết
				foreach (var validationErrors in ex.EntityValidationErrors)
				{
					foreach (var validationError in validationErrors.ValidationErrors)
					{
						System.Diagnostics.Debug.WriteLine($"Property: {validationError.PropertyName} Error: {validationError.ErrorMessage}");
					}
				}
				ViewBag.Notification = "Cập nhật thất bại, vui lòng kiểm tra lại thông tin!";
				return View(updatedAccount); // Trả về form nếu có lỗi
			}
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
