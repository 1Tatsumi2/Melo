using MusicApp.Models;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Mail;
using System.Net;
using System.Web;
using System.Web.Mvc;

namespace MusicApp.Controllers
{
    public class SignInUpController : Controller
    {
        DAPMMainEntities db = new DAPMMainEntities();
        private readonly DAPMMainEntities _context;
        // GET: SignInUp
        public SignInUpController() : this(new DAPMMainEntities())
        {

        }

        public SignInUpController(DAPMMainEntities context)
        {
            _context = context;
        }

        public ActionResult Index()
        {
            return View(db.Accounts.ToList());
        }

        public ActionResult SignUp()
        {
            return View();
        }

        [HttpPost]
        public ActionResult SignUp(Account account, HttpPostedFileBase HinhAnh)
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


                return RedirectToAction("TrangChu", "Home");
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

        public ActionResult Logout()
        {
            Session.Clear();
            return RedirectToAction("Login", "SignInUp");
        }

        [HttpGet]
        public ActionResult Login()
        {
            return View();
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public ActionResult Login(Account account)
        {
            var checkLogin = db.Accounts.Where(x => x.TaiKhoan.Equals(account.TaiKhoan) && x.MatKhau.Equals(account.MatKhau)).FirstOrDefault();
            if (checkLogin != null)
            {
                Session["Ma_User"] = checkLogin.Ma_User.ToString();
                Session["TaiKhoan"] = checkLogin.TaiKhoan.ToString();
                Session["UserName"] = checkLogin.UserName.ToString();
                Session["HinhAnh"] = checkLogin.HinhAnh.ToString();
                Session["Role"] = checkLogin.Role.ToString();
                Session["Email"] = checkLogin.Email.ToString();

                return RedirectToAction("TrangChu", "Home");
            }
            else
            {
                ViewBag.Notification = "Sai tài khoản hoặc mật khẩu";
            }    
            return View();
        }

        public ActionResult ResetPassword()
        {
            return View();
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public ActionResult ResetPassword(string email)
        {
            // Kiểm tra xem email có trong cơ sở dữ liệu không
            var account = db.Accounts.FirstOrDefault(a => a.Email == email);
            if (account == null)
            {
                ViewBag.Error = "Email không tồn tại!";
                return View();
            }

            // Tạo một token để gửi qua email (có thể là mã ngẫu nhiên hoặc mã xác thực)
            var token = Guid.NewGuid().ToString();

            Session["ResetPasswordToken"] = token; // Lưu token vào session
            Session["ResetPasswordEmail"] = email; // Lưu email vào session

            // Gửi email
            SendResetPasswordEmail(account.Email, token);

            ViewBag.Message = "Đường link reset password đã được gửi đến email của bạn!";
            return View();
        }

        private void SendResetPasswordEmail(string email, string token)
        {
            var fromAddress = new MailAddress("tranthuong1212@gmail.com", "MusicApp");
            var toAddress = new MailAddress(email);
            const string fromPassword = "yxoe qaec uuqj eiir"; // Mật khẩu for app của Gmail
            const string subject = "Reset Password";
            string body = $"Click vào link dưới đây để reset mật khẩu:\n" +
                          $"https://localhost:44371/SignInUp/ResetPasswordConfirm?token={token}";

            var smtp = new SmtpClient
            {
                Host = "smtp.gmail.com", 
                Port = 587, 
                EnableSsl = true,
                DeliveryMethod = SmtpDeliveryMethod.Network,
                UseDefaultCredentials = false,
                Credentials = new NetworkCredential(fromAddress.Address, fromPassword)
            };

            using (var message = new MailMessage(fromAddress, toAddress)
            {
                Subject = subject,
                Body = body
            })
            {
                smtp.Send(message);
            }
        }

        //public void GenerateResetToken(string email)
        //{
        //    var token = Guid.NewGuid().ToString(); // Tạo token
        //    Session["ResetPasswordToken"] = token; // Lưu token vào session
        //    Session["ResetPasswordEmail"] = email; // Lưu email vào session
        //    SendResetPasswordEmail(email, token); // Gửi email với token
        //}

        [HttpGet]
        public ActionResult ResetPasswordConfirm(string token)
        {
            var storedToken = Session["ResetPasswordToken"] as string;

            if (storedToken == null)
            {
                System.IO.File.AppendAllText(Server.MapPath("~/App_Data/ErrorLog.txt"), "Token trong session không tồn tại.\n");
                return View("Error"); // Nếu không tìm thấy token trong session, trả về lỗi
            }

            if (storedToken != token)
            {
                System.IO.File.AppendAllText(Server.MapPath("~/App_Data/ErrorLog.txt"), $"Token trong session không khớp: {storedToken} != {token}\n");
                return View("Error"); // Nếu token không khớp, trả về lỗi
            }

            ViewBag.Token = token; // Gửi token cho view
            System.IO.File.AppendAllText(Server.MapPath("~/App_Data/ErrorLog.txt"), $"Token khớp: {token}\n");
            return View();
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public ActionResult ResetPasswordConfirm(string token, string newPassword)
        {
            var storedToken = Session["ResetPasswordToken"] as string;
            var email = Session["ResetPasswordEmail"] as string;

            if (storedToken == null)
            {
                System.IO.File.AppendAllText(Server.MapPath("~/App_Data/ErrorLog.txt"), "Token trong session không tồn tại trong POST request.\n");
                return View("Error"); // Token không tồn tại trong session
            }

            if (storedToken != token)
            {
                System.IO.File.AppendAllText(Server.MapPath("~/App_Data/ErrorLog.txt"), $"Token không khớp trong POST request: {storedToken} != {token}\n");
                return View("Error"); // Token không khớp
            }

            if (email == null)
            {
                System.IO.File.AppendAllText(Server.MapPath("~/App_Data/ErrorLog.txt"), "Email trong session không tồn tại trong POST request.\n");
                return View("Error"); // Email không tồn tại trong session
            }

            try
            {
                var account = _context.Accounts.FirstOrDefault(a => a.Email == email);
                if (account == null)
                {
                    System.IO.File.AppendAllText(Server.MapPath("~/App_Data/ErrorLog.txt"), "Không tìm thấy tài khoản với email: " + email + "\n");
                    ModelState.AddModelError("", "Tài khoản không tồn tại.");
                    return View("Error");
                }

                account.MatKhau = newPassword; // Hash mật khẩu nếu cần
                account.NhapLaiMatKhau = newPassword; // Hash mật khẩu nếu cần
                _context.SaveChanges(); // Lưu thay đổi vào cơ sở dữ liệu
                System.IO.File.AppendAllText(Server.MapPath("~/App_Data/ErrorLog.txt"), "Đã cập nhật mật khẩu thành công cho email: " + email + "\n");
            }
            catch (Exception ex)
            {
                System.IO.File.AppendAllText(Server.MapPath("~/App_Data/ErrorLog.txt"), "Lỗi khi cập nhật mật khẩu: " + ex.ToString() + "\n");
                ModelState.AddModelError("", "Đã xảy ra lỗi trong quá trình thay đổi mật khẩu. Vui lòng thử lại sau.");
                return View("Error");
            }

            // Xóa token và email khỏi session
            Session.Remove("ResetPasswordToken");
            Session.Remove("ResetPasswordEmail");

            return RedirectToAction("Login", "SignInUp");
        }




    }
}