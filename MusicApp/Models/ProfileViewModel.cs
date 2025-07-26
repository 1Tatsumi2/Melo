using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace MusicApp.Models
{
	public class ProfileViewModel
	{
		public Account User { get; set; } // Thông tin người dùng
		public List<Singer> TopArtists { get; set; } // Danh sách ca sĩ
		public string TaiKhoan { get; set; }
		public string Email { get; set; }
		public string MatKhau { get; set; }
		public string UserName { get; set; }
		public string HinhAnh { get; set; }
		public int Role { get; set; }
		public int Ma_User { get; set; }
	}
}